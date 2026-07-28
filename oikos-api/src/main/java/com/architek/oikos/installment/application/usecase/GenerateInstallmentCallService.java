package com.architek.oikos.installment.application.usecase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.dto.InstallmentCallView;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;
import com.architek.oikos.installment.application.port.in.GenerateInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.PropertyUnitPricingPort;
import com.architek.oikos.installment.application.port.out.UnitPriceLine;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.exception.InstallmentCallAlreadyExistsException;
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
 * Installment per unit, priced from UnitTypePricing. A unit is skipped and
 * reported rather than blocking the whole property when its type has no
 * configured price (not an error - see UnitTypePricing).
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

        List<UnitPriceLine> unitPrices = propertyUnitPricingPort.listUnitPrices(command.propertyId());
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
}
