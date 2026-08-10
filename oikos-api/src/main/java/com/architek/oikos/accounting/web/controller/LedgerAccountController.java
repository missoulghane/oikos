package com.architek.oikos.accounting.web.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.accounting.application.port.in.ListLedgerAccountsByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListLedgerAccountsByPropertyQuery;
import com.architek.oikos.accounting.web.response.LedgerAccountResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Read-only referentiel endpoint (spec S7.1: GET /referentiel/comptes), scoped to one property. */
@RestController
public class LedgerAccountController {

    private final ListLedgerAccountsByPropertyUseCase listLedgerAccountsByPropertyUseCase;

    public LedgerAccountController(ListLedgerAccountsByPropertyUseCase listLedgerAccountsByPropertyUseCase) {
        this.listLedgerAccountsByPropertyUseCase = listLedgerAccountsByPropertyUseCase;
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/ledger-accounts")
    public List<LedgerAccountResponse> list(@PathVariable String propertyId) {
        return listLedgerAccountsByPropertyUseCase.list(new ListLedgerAccountsByPropertyQuery(EntityId.of(propertyId)))
                .stream()
                .map(LedgerAccountResponse::from)
                .toList();
    }
}
