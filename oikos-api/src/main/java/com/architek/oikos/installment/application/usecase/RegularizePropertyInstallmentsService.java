package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.RegularizePropertyInstallmentsCommand;
import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizePropertyInstallmentsResult;
import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;
import com.architek.oikos.installment.application.port.in.RegularizePropertyInstallmentsUseCase;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.application.port.out.AdvanceRegularizationPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.domain.exception.NothingToRegularizeException;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;

/**
 * Property-wide sweep: only asks accounting for units that actually carry a
 * positive advance (AdvanceRegularizationPort.listUnitsWithAvailableAdvance),
 * so most units - which have nothing to regularize - are never even looked
 * at, rather than walking every unit of the property and mostly no-oping.
 * A unit with an advance but no unsettled call left (NothingToRegularizeException)
 * is skipped, not fatal to the sweep - that is a legitimate outcome for most
 * units, not an error.
 */
@Component
public class RegularizePropertyInstallmentsService implements RegularizePropertyInstallmentsUseCase {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final AdvanceRegularizationPort advanceRegularizationPort;
    private final RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;

    public RegularizePropertyInstallmentsService(PropertyDirectoryPort propertyDirectoryPort,
                                                  AdvanceRegularizationPort advanceRegularizationPort,
                                                  RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.advanceRegularizationPort = advanceRegularizationPort;
        this.regularizeUnitInstallmentsUseCase = regularizeUnitInstallmentsUseCase;
    }

    @Override
    @Transactional
    public RegularizePropertyInstallmentsResult regularize(RegularizePropertyInstallmentsCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }

        List<RegularizeUnitInstallmentsResult> regularized = new ArrayList<>();
        for (AdvanceRegularizationPort.UnitAdvance unitAdvance : advanceRegularizationPort
                .listUnitsWithAvailableAdvance(command.propertyId())) {
            try {
                regularized.add(regularizeUnitInstallmentsUseCase.regularize(new RegularizeUnitInstallmentsCommand(
                        command.propertyId(), unitAdvance.unitId(), command.pieceDate(), command.createdByUserId())));
            } catch (NothingToRegularizeException noUnsettledCallLeft) {
                // Has an advance but nothing left to impute it on (e.g. already fully settled
                // by a later payment) - a legitimate outcome for the sweep, not an error.
            }
        }

        BigDecimal totalAmountApplied = regularized.stream()
                .map(RegularizeUnitInstallmentsResult::amountApplied)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RegularizePropertyInstallmentsResult(regularized, totalAmountApplied);
    }
}
