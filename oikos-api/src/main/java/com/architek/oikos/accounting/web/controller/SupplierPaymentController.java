package com.architek.oikos.accounting.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.RecordSupplierPaymentCommand;
import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.accounting.application.port.in.RecordSupplierPaymentUseCase;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.web.request.RecordSupplierPaymentRequest;
import com.architek.oikos.accounting.web.response.ExpenseResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** P4/P5 merged (spec &sect;6, Partie 2): direct supplier payments. */
@RestController
public class SupplierPaymentController {

    private final RecordSupplierPaymentUseCase recordSupplierPaymentUseCase;

    public SupplierPaymentController(RecordSupplierPaymentUseCase recordSupplierPaymentUseCase) {
        this.recordSupplierPaymentUseCase = recordSupplierPaymentUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/supplier-payments")
    public ResponseEntity<ExpenseResponse> record(@PathVariable String propertyId,
                                                   @Valid @RequestBody RecordSupplierPaymentRequest request,
                                                   Authentication authentication) {
        ExpenseView view = recordSupplierPaymentUseCase.record(new RecordSupplierPaymentCommand(
                EntityId.of(propertyId), request.pieceDate(), LedgerAccountId.of(request.ledgerAccountId()),
                LedgerAccountId.of(request.treasuryAccountId()), request.amount(), request.description(),
                request.externalReference(), EntityId.of(authentication.getName())));
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/accounting/expenses/" + view.id()))
                .body(ExpenseResponse.from(view));
    }
}
