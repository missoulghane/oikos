package com.architek.oikos.accounting.web.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.accounting.application.port.in.GetTreasurySummaryUseCase;
import com.architek.oikos.accounting.application.port.in.ListFinancialJournalEntriesUseCase;
import com.architek.oikos.accounting.application.port.in.ListUnitAccountSummariesByPropertyUseCase;
import com.architek.oikos.accounting.application.query.GetTreasurySummaryQuery;
import com.architek.oikos.accounting.application.query.ListFinancialJournalEntriesQuery;
import com.architek.oikos.accounting.application.query.ListUnitAccountSummariesByPropertyQuery;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryFilter;
import com.architek.oikos.accounting.web.response.PagedFinancialJournalEntryResponse;
import com.architek.oikos.accounting.web.response.TreasurySummaryResponse;
import com.architek.oikos.accounting.web.response.UnitAccountSummaryResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Spec &sect;16 "Tableaux de suivi": tresorerie, situation des units, journal filtre. */
@RestController
public class AccountingReportController {

    private final GetTreasurySummaryUseCase getTreasurySummaryUseCase;
    private final ListUnitAccountSummariesByPropertyUseCase listUnitAccountSummariesByPropertyUseCase;
    private final ListFinancialJournalEntriesUseCase listFinancialJournalEntriesUseCase;

    public AccountingReportController(GetTreasurySummaryUseCase getTreasurySummaryUseCase,
                                       ListUnitAccountSummariesByPropertyUseCase listUnitAccountSummariesByPropertyUseCase,
                                       ListFinancialJournalEntriesUseCase listFinancialJournalEntriesUseCase) {
        this.getTreasurySummaryUseCase = getTreasurySummaryUseCase;
        this.listUnitAccountSummariesByPropertyUseCase = listUnitAccountSummariesByPropertyUseCase;
        this.listFinancialJournalEntriesUseCase = listFinancialJournalEntriesUseCase;
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/treasury-summary")
    public TreasurySummaryResponse getTreasurySummary(@PathVariable String propertyId) {
        return TreasurySummaryResponse.from(
                getTreasurySummaryUseCase.get(new GetTreasurySummaryQuery(EntityId.of(propertyId))));
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/units-summary")
    public List<UnitAccountSummaryResponse> listUnitAccountSummaries(@PathVariable String propertyId) {
        return listUnitAccountSummariesByPropertyUseCase
                .list(new ListUnitAccountSummariesByPropertyQuery(EntityId.of(propertyId))).stream()
                .map(UnitAccountSummaryResponse::from)
                .toList();
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/journal")
    public PagedFinancialJournalEntryResponse listJournal(@PathVariable String propertyId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(required = false) String exerciseId,
                                                           @RequestParam(required = false) String financialAccountId,
                                                           @RequestParam(required = false) FinancialEntryType type,
                                                           @RequestParam(required = false) LocalDate dateFrom,
                                                           @RequestParam(required = false) LocalDate dateTo) {
        FinancialJournalEntryFilter filter = new FinancialJournalEntryFilter(
                exerciseId == null ? null : AccountingExerciseId.of(exerciseId),
                financialAccountId == null ? null : FinancialAccountId.of(financialAccountId), type, dateFrom, dateTo);
        return PagedFinancialJournalEntryResponse.from(listFinancialJournalEntriesUseCase.list(
                new ListFinancialJournalEntriesQuery(EntityId.of(propertyId), filter, PageRequest.of(page, size))));
    }
}
