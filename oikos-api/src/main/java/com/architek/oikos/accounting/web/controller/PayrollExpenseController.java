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
import com.architek.oikos.accounting.application.command.RecordPayrollExpenseCommand;
import com.architek.oikos.accounting.application.port.in.RecordPayrollExpenseUseCase;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.web.request.RecordPayrollExpenseRequest;
import com.architek.oikos.accounting.web.response.JournalEntryReferenceResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** P6 (spec &sect;6): personnel charge accruals. */
@RestController
public class PayrollExpenseController {

    private final RecordPayrollExpenseUseCase recordPayrollExpenseUseCase;

    public PayrollExpenseController(RecordPayrollExpenseUseCase recordPayrollExpenseUseCase) {
        this.recordPayrollExpenseUseCase = recordPayrollExpenseUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/payroll-expenses")
    public ResponseEntity<JournalEntryReferenceResponse> record(@PathVariable String propertyId,
                                                                 @Valid @RequestBody RecordPayrollExpenseRequest request,
                                                                 Authentication authentication) {
        JournalEntryId journalEntryId = recordPayrollExpenseUseCase.record(new RecordPayrollExpenseCommand(
                EntityId.of(propertyId), request.date(), LedgerAccountId.of(request.ledgerAccountId()),
                request.amount(), request.description(), EntityId.of(authentication.getName())));
        return ResponseEntity.status(HttpStatus.CREATED).body(JournalEntryReferenceResponse.from(journalEntryId));
    }
}
