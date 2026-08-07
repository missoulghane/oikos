package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.dto.InstallmentCallView;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;
import com.architek.oikos.installment.application.port.in.GenerateInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.PropertyDuesConfigurationView;
import com.architek.oikos.installment.application.port.out.PropertyUnitPricingPort;
import com.architek.oikos.installment.application.port.out.UnitPriceLine;
import com.architek.oikos.installment.application.port.out.UnitShareLine;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.exception.InstallmentCallAlreadyExistsException;
import com.architek.oikos.installment.domain.exception.NoUnitSharesConfiguredException;
import com.architek.oikos.installment.domain.exception.ProjectedBudgetNotConfiguredException;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Generates a full installment call for a property in one batch: one
 * Installment per unit, priced according to the property's dues calculation
 * mode - FLAT_RATE (price from UnitTypePricing) or SHARES (property's
 * projected budget prorated by each unit's shares/tantiemes). A unit is
 * skipped and reported rather than blocking the whole property when it has
 * no basis to be charged (no price configured for its type, or zero shares -
 * not an error either way, see UnitTypePricing / Unit.shares).
 */
@Component
public class GenerateInstallmentCallService implements GenerateInstallmentCallUseCase {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final PropertyUnitPricingPort propertyUnitPricingPort;
    private final InstallmentCallRepository installmentCallRepository;
    private final InstallmentRepository installmentRepository;

    public GenerateInstallmentCallService(PropertyDirectoryPort propertyDirectoryPort,
                                         PropertyUnitPricingPort propertyUnitPricingPort,
                                         InstallmentCallRepository installmentCallRepository,
                                         InstallmentRepository installmentRepository) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.propertyUnitPricingPort = propertyUnitPricingPort;
        this.installmentCallRepository = installmentCallRepository;
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional
    public GenerateInstallmentCallResult generate(GenerateInstallmentCallCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }
        if (installmentCallRepository.existsByPropertyIdAndPeriod(command.propertyId(), command.period())) {
            throw new InstallmentCallAlreadyExistsException(command.propertyId(), command.period());
        }

        PropertyDuesConfigurationView duesConfiguration = propertyDirectoryPort.getDuesConfiguration(command.propertyId());
        List<UnitPriceLine> unitPrices = switch (duesConfiguration.mode()) {
            case FLAT_RATE -> propertyUnitPricingPort.listUnitPrices(command.propertyId());
            case SHARES -> resolveShareBasedAmounts(command.propertyId(), duesConfiguration.projectedBudget());
        };
        List<UnitPriceLine> priced = unitPrices.stream().filter(line -> line.price() != null).toList();
        List<EntityId> skippedUnitIds = new ArrayList<>(unitPrices.stream().filter(line -> line.price() == null)
                .map(UnitPriceLine::unitId).toList());

        InstallmentCall savedCall = installmentCallRepository.save(
                InstallmentCall.create(InstallmentCallId.newId(), command.propertyId(), command.period(), command.dueDate()));

        List<EntityId> chargedUnitIds = new ArrayList<>();
        for (UnitPriceLine line : priced) {
            Installment installment = Installment.create(InstallmentId.newId(), line.unitId(),
                    command.dueDate(), Amount.of(line.price()), savedCall.getId());
            installmentRepository.save(installment);

            chargedUnitIds.add(line.unitId());
        }

        return new GenerateInstallmentCallResult(InstallmentCallView.from(savedCall), chargedUnitIds, skippedUnitIds);
    }

    /**
     * Prorates projectedBudget across every unit in proportion to its shares
     * (tantiemes). A unit with zero shares gets a null price (skipped, same
     * convention as FLAT_RATE's unpriced units). Per-unit amounts are rounded
     * to 2 decimals, except the last shared unit which absorbs the rounding
     * remainder so the sum of charged amounts always equals projectedBudget
     * exactly - required for the budget to reconcile with what is actually
     * called.
     */
    private List<UnitPriceLine> resolveShareBasedAmounts(EntityId propertyId, BigDecimal projectedBudget) {
        if (projectedBudget == null) {
            throw new ProjectedBudgetNotConfiguredException(propertyId);
        }

        List<UnitShareLine> unitShares = propertyUnitPricingPort.listUnitShares(propertyId);
        BigDecimal totalShares = unitShares.stream().map(UnitShareLine::shares).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalShares.signum() <= 0) {
            throw new NoUnitSharesConfiguredException(propertyId);
        }

        List<UnitShareLine> sharedUnits = unitShares.stream().filter(line -> line.shares().signum() > 0).toList();
        List<UnitPriceLine> lines = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < sharedUnits.size(); i++) {
            UnitShareLine line = sharedUnits.get(i);
            BigDecimal amount = i == sharedUnits.size() - 1
                    ? projectedBudget.subtract(allocated)
                    : projectedBudget.multiply(line.shares()).divide(totalShares, 2, RoundingMode.HALF_UP);
            allocated = allocated.add(amount);
            lines.add(new UnitPriceLine(line.unitId(), amount));
        }
        unitShares.stream().filter(line -> line.shares().signum() <= 0)
                .forEach(line -> lines.add(new UnitPriceLine(line.unitId(), null)));
        return lines;
    }
}
