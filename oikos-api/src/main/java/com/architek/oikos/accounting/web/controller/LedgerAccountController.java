package com.architek.oikos.accounting.web.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.accounting.application.port.in.ListLedgerAccountsByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.ListJournalEntriesByTreasuryAccountUseCase;
import com.architek.oikos.accounting.application.query.ListJournalEntriesByTreasuryAccountQuery;
import com.architek.oikos.accounting.application.query.ListLedgerAccountsByPropertyQuery;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.web.response.LedgerAccountResponse;
import com.architek.oikos.accounting.web.response.PagedJournalEntryResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Read-only referentiel endpoint (spec S7.1: GET /referentiel/comptes), scoped to one property. */
@RestController
public class LedgerAccountController {

    private final ListLedgerAccountsByPropertyUseCase listLedgerAccountsByPropertyUseCase;
    private final ListJournalEntriesByTreasuryAccountUseCase listJournalEntriesByTreasuryAccountUseCase;

    public LedgerAccountController(ListLedgerAccountsByPropertyUseCase listLedgerAccountsByPropertyUseCase,
                                    ListJournalEntriesByTreasuryAccountUseCase listJournalEntriesByTreasuryAccountUseCase) {
        this.listLedgerAccountsByPropertyUseCase = listLedgerAccountsByPropertyUseCase;
        this.listJournalEntriesByTreasuryAccountUseCase = listJournalEntriesByTreasuryAccountUseCase;
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/ledger-accounts")
    public List<LedgerAccountResponse> list(@PathVariable String propertyId) {
        return listLedgerAccountsByPropertyUseCase.list(new ListLedgerAccountsByPropertyQuery(EntityId.of(propertyId)))
                .stream()
                .map(LedgerAccountResponse::from)
                .toList();
    }

    /** Operations of a single treasury account (accounting overview "click an account" flow). */
    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/ledger-accounts/{accountId}/entries")
    public PagedJournalEntryResponse listEntries(@PathVariable String propertyId, @PathVariable String accountId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pieceDateFrom,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pieceDateTo,
                                                  @RequestParam(required = false) String search,
                                                  @RequestParam(required = false) JournalEntryStatus status) {
        JournalEntryFilter filter = new JournalEntryFilter(pieceDateFrom, pieceDateTo, search, status);
        return PagedJournalEntryResponse.from(listJournalEntriesByTreasuryAccountUseCase.list(
                new ListJournalEntriesByTreasuryAccountQuery(EntityId.of(propertyId), LedgerAccountId.of(accountId),
                        filter, PageRequest.of(page, size))));
    }
}
