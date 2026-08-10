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
import com.architek.oikos.accounting.application.command.RecordTreasuryTransferCommand;
import com.architek.oikos.accounting.application.port.in.RecordTreasuryTransferUseCase;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.web.request.RecordTreasuryTransferRequest;
import com.architek.oikos.accounting.web.response.JournalEntryReferenceResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Mouvement entre comptes de tresorerie (virement caisse<->banque, ou entre deux comptes bancaires). */
@RestController
public class TreasuryTransferController {

    private final RecordTreasuryTransferUseCase recordTreasuryTransferUseCase;

    public TreasuryTransferController(RecordTreasuryTransferUseCase recordTreasuryTransferUseCase) {
        this.recordTreasuryTransferUseCase = recordTreasuryTransferUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/treasury-transfers")
    public ResponseEntity<JournalEntryReferenceResponse> record(@PathVariable String propertyId,
                                                                  @Valid @RequestBody RecordTreasuryTransferRequest request,
                                                                  Authentication authentication) {
        JournalEntryId journalEntryId = recordTreasuryTransferUseCase.record(new RecordTreasuryTransferCommand(
                EntityId.of(propertyId), LedgerAccountId.of(request.sourceAccountId()),
                LedgerAccountId.of(request.destinationAccountId()), request.pieceDate(), request.amount(),
                request.description(), EntityId.of(authentication.getName())));
        return ResponseEntity.status(HttpStatus.CREATED).body(JournalEntryReferenceResponse.from(journalEntryId));
    }
}
