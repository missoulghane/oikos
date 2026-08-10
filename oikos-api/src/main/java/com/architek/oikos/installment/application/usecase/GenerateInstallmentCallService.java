package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.dto.FundCallLine;
import com.architek.oikos.installment.application.dto.InstallmentCallView;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;
import com.architek.oikos.installment.application.port.in.GenerateInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.out.FundCallJournalEntryPort;
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
import com.architek.oikos.installment.domain.model.SharesApportionment;
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
    private final FundCallJournalEntryPort fundCallJournalEntryPort;

    public GenerateInstallmentCallService(PropertyDirectoryPort propertyDirectoryPort,
                                         PropertyUnitPricingPort propertyUnitPricingPort,
                                         InstallmentCallRepository installmentCallRepository,
                                         InstallmentRepository installmentRepository,
                                         FundCallJournalEntryPort fundCallJournalEntryPort) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.propertyUnitPricingPort = propertyUnitPricingPort;
        this.installmentCallRepository = installmentCallRepository;
        this.installmentRepository = installmentRepository;
        this.fundCallJournalEntryPort = fundCallJournalEntryPort;
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

        InstallmentCallId callId = InstallmentCallId.newId();
        InstallmentCall call = priced.isEmpty()
                ? InstallmentCall.create(callId, command.propertyId(), command.period(), command.dueDate())
                : InstallmentCall.draft(callId, command.propertyId(), command.period(), command.dueDate()).issue();
        InstallmentCall savedCall = installmentCallRepository.save(call);

        List<EntityId> chargedUnitIds = new ArrayList<>();
        List<FundCallLine> fundCallLines = new ArrayList<>();
        for (UnitPriceLine line : priced) {
            Installment installment = Installment.create(InstallmentId.newId(), line.unitId(),
                    command.dueDate(), Amount.of(line.price()), savedCall.getId());
            installmentRepository.save(installment);

            chargedUnitIds.add(line.unitId());
            fundCallLines.add(new FundCallLine(line.unitId(), line.price()));
        }

        if (!fundCallLines.isEmpty()) {
            // P1 (spec S6): the entry is dated on the period being billed, not the
            // (possibly later) due date.
            EntityId journalEntryId = fundCallJournalEntryPort.postFundCallEntry(command.propertyId(),
                    command.period().atDay(1), "Appel de fonds " + command.period(), command.createdByUserId(),
                    fundCallLines);
            savedCall = installmentCallRepository.save(savedCall.post(journalEntryId));
        }

        return new GenerateInstallmentCallResult(InstallmentCallView.from(savedCall), chargedUnitIds, skippedUnitIds);
    }

    /**
     * Prorates projectedBudget across every unit in proportion to its shares
     * (tantiemes), using the largest-remainder method (I9 -
     * SharesApportionment) so the rounding cents are spread deterministically
     * across the units with the largest fractional remainder rather than
     * dumped onto a single arbitrary unit, while the sum of charged amounts
     * still equals projectedBudget exactly. A unit with zero shares gets a
     * null price (skipped, same convention as FLAT_RATE's unpriced units).
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

        List<SharesApportionment.Share> sharedUnits = unitShares.stream()
                .filter(line -> line.shares().signum() > 0)
                .map(line -> new SharesApportionment.Share(line.unitId(), line.shares()))
                .toList();
        List<SharesApportionment.Allocation> allocations = SharesApportionment.apportion(projectedBudget, sharedUnits);

        List<UnitPriceLine> lines = new ArrayList<>();
        allocations.forEach(allocation -> lines.add(new UnitPriceLine(allocation.unitId(), allocation.amount())));
        unitShares.stream().filter(line -> line.shares().signum() <= 0)
                .forEach(line -> lines.add(new UnitPriceLine(line.unitId(), null)));
        return lines;
    }
}
