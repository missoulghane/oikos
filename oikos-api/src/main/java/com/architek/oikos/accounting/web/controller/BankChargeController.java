package com.architek.oikos.accounting.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.RecordBankChargeCommand;
import com.architek.oikos.accounting.application.port.in.RecordBankChargeUseCase;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.web.request.RecordBankChargeRequest;
import com.architek.oikos.accounting.web.response.JournalEntryReferenceResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** P7 (spec &sect;6): bank fees deducted directly by the bank. */
@RestController
public class BankChargeController {

    private final RecordBankChargeUseCase recordBankChargeUseCase;

    public BankChargeController(RecordBankChargeUseCase recordBankChargeUseCase) {
        this.recordBankChargeUseCase = recordBankChargeUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/bank-charges")
    public ResponseEntity<JournalEntryReferenceResponse> record(@PathVariable String propertyId,
                                                                 @Valid @RequestBody RecordBankChargeRequest request,
                                                                 Authentication authentication) {
        JournalEntryId journalEntryId = recordBankChargeUseCase.record(new RecordBankChargeCommand(
                EntityId.of(propertyId), request.pieceDate(), LedgerAccountId.of(request.ledgerAccountId()),
                LedgerAccountId.of(request.bankAccountId()), request.amount(), request.description(),
                EntityId.of(authentication.getName())));
        return ResponseEntity.status(HttpStatus.CREATED).body(JournalEntryReferenceResponse.from(journalEntryId));
    }
}
