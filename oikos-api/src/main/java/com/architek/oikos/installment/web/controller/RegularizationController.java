package com.architek.oikos.installment.web.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.installment.application.command.RegularizePropertyInstallmentsCommand;
import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.port.in.RegularizePropertyInstallmentsUseCase;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.web.response.RegularizePropertyInstallmentsResponse;
import com.architek.oikos.installment.web.response.RegularizeUnitInstallmentsResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Regularisation ("lettrage"): imputes a unit's already-recorded, still
 * unclaimed advance onto its unpaid fund calls - the gap identified when a
 * payment is recorded before the fund call it should settle exists yet
 * (PaymentAllocationCalculator only ever looks at calls present at payment
 * time, never retroactively). Same write permission as recording an owner
 * payment (installment:call:write), since this produces the same kind of
 * entry a payment would.
 */
@RestController
public class RegularizationController {

    private final RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;
    private final RegularizePropertyInstallmentsUseCase regularizePropertyInstallmentsUseCase;

    public RegularizationController(RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase,
                                     RegularizePropertyInstallmentsUseCase regularizePropertyInstallmentsUseCase) {
        this.regularizeUnitInstallmentsUseCase = regularizeUnitInstallmentsUseCase;
        this.regularizePropertyInstallmentsUseCase = regularizePropertyInstallmentsUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteInstallmentCall(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/units/{unitId}/installments/regularization")
    public RegularizeUnitInstallmentsResponse regularizeUnit(@PathVariable String propertyId,
                                                              @PathVariable String unitId,
                                                              Authentication authentication) {
        return RegularizeUnitInstallmentsResponse.from(regularizeUnitInstallmentsUseCase.regularize(
                new RegularizeUnitInstallmentsCommand(EntityId.of(propertyId), EntityId.of(unitId), LocalDate.now(),
                        EntityId.of(authentication.getName()))));
    }

    @PreAuthorize("@propertyAccess.canWriteInstallmentCall(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/installments/regularization")
    public RegularizePropertyInstallmentsResponse regularizeProperty(@PathVariable String propertyId,
                                                                       Authentication authentication) {
        return RegularizePropertyInstallmentsResponse.from(regularizePropertyInstallmentsUseCase.regularize(
                new RegularizePropertyInstallmentsCommand(EntityId.of(propertyId), LocalDate.now(),
                        EntityId.of(authentication.getName()))));
    }
}
