package com.architek.oikos.installment.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.installment.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.installment.application.port.in.ListPaymentsByUnitUseCase;
import com.architek.oikos.installment.application.port.in.RecordOwnerPaymentUseCase;
import com.architek.oikos.installment.application.query.ListPaymentsByUnitQuery;
import com.architek.oikos.installment.web.request.RecordOwnerPaymentRequest;
import com.architek.oikos.installment.web.response.PaymentResponse;
import com.architek.oikos.installment.web.response.RecordOwnerPaymentResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class PaymentController {

    private final RecordOwnerPaymentUseCase recordOwnerPaymentUseCase;
    private final ListPaymentsByUnitUseCase listPaymentsByUnitUseCase;

    public PaymentController(RecordOwnerPaymentUseCase recordOwnerPaymentUseCase,
                              ListPaymentsByUnitUseCase listPaymentsByUnitUseCase) {
        this.recordOwnerPaymentUseCase = recordOwnerPaymentUseCase;
        this.listPaymentsByUnitUseCase = listPaymentsByUnitUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteInstallmentCall(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/units/{unitId}/payments")
    public ResponseEntity<RecordOwnerPaymentResponse> record(@PathVariable String propertyId,
                                                              @PathVariable String unitId,
                                                              @Valid @RequestBody RecordOwnerPaymentRequest request,
                                                              Authentication authentication) {
        RecordOwnerPaymentResponse response = RecordOwnerPaymentResponse.from(recordOwnerPaymentUseCase.record(
                new RecordOwnerPaymentCommand(EntityId.of(propertyId), EntityId.of(unitId), request.mode(),
                        EntityId.of(request.treasuryAccountId()), request.valueDate(), request.amount(),
                        EntityId.of(authentication.getName()))));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId) or @propertyAccess.ownsUnit(authentication, #unitId)")
    @GetMapping("/units/{unitId}/payments")
    public List<PaymentResponse> listByUnit(@PathVariable String unitId) {
        return listPaymentsByUnitUseCase.listPayments(new ListPaymentsByUnitQuery(EntityId.of(unitId))).stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
