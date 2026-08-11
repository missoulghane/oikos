package com.architek.oikos.accounting.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.AddBankAccountCommand;
import com.architek.oikos.accounting.application.port.in.AddBankAccountUseCase;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.web.request.AddBankAccountRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Manual bank account configuration ("exigence supplementaire": bank
 * accounts are set up after the property is created, unlike the cash
 * account). The account itself is then visible like any other through the
 * existing read-only GET /properties/{propertyId}/accounting/ledger-accounts.
 */
@RestController
public class BankAccountController {

    private final AddBankAccountUseCase addBankAccountUseCase;

    public BankAccountController(AddBankAccountUseCase addBankAccountUseCase) {
        this.addBankAccountUseCase = addBankAccountUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/bank-accounts")
    public ResponseEntity<Void> add(@PathVariable String propertyId, @Valid @RequestBody AddBankAccountRequest request) {
        LedgerAccountId id = addBankAccountUseCase.add(new AddBankAccountCommand(EntityId.of(propertyId), request.label(), request.bankAccountNumber()));
        return ResponseEntity.created(
                        URI.create("/api/v1/properties/" + propertyId + "/accounting/ledger-accounts/" + id))
                .build();
    }
}
