package com.architek.oikos.accounting.web.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.GetJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.ListJournalEntriesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.query.GetJournalEntryQuery;
import com.architek.oikos.accounting.application.query.ListJournalEntriesByPropertyQuery;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.web.request.CreateJournalEntryDraftRequest;
import com.architek.oikos.accounting.web.request.CreateJournalEntryLineRequest;
import com.architek.oikos.accounting.web.response.JournalEntryResponse;
import com.architek.oikos.accounting.web.response.PagedJournalEntryResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Spec &sect;7.3: generic double-entry journal entries, scoped to a property (existing oikos convention). */
@RestController
public class JournalEntryController {

    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;
    private final GetJournalEntryUseCase getJournalEntryUseCase;
    private final ListJournalEntriesByPropertyUseCase listJournalEntriesByPropertyUseCase;

    public JournalEntryController(CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase,
                                   PostJournalEntryUseCase postJournalEntryUseCase,
                                   GetJournalEntryUseCase getJournalEntryUseCase,
                                   ListJournalEntriesByPropertyUseCase listJournalEntriesByPropertyUseCase) {
        this.createJournalEntryDraftUseCase = createJournalEntryDraftUseCase;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
        this.getJournalEntryUseCase = getJournalEntryUseCase;
        this.listJournalEntriesByPropertyUseCase = listJournalEntriesByPropertyUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/entries")
    public ResponseEntity<Void> create(@PathVariable String propertyId,
                                        @Valid @RequestBody CreateJournalEntryDraftRequest request,
                                        Authentication authentication) {
        List<CreateJournalEntryLineCommand> lines = request.lines().stream()
                .map(this::toLineCommand)
                .toList();
        JournalEntryId id = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                EntityId.of(propertyId), request.journalCode(),
                request.treasuryAccountId() == null ? null : LedgerAccountId.of(request.treasuryAccountId()),
                request.pieceDate(), request.externalReference(), EntityId.of(authentication.getName()), lines));
        return ResponseEntity.created(
                        URI.create("/api/v1/properties/" + propertyId + "/accounting/entries/" + id))
                .build();
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/entries/{entryId}")
    public JournalEntryResponse getById(@PathVariable String propertyId, @PathVariable String entryId) {
        return JournalEntryResponse.from(
                getJournalEntryUseCase.get(new GetJournalEntryQuery(JournalEntryId.of(entryId))));
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/accounting/entries")
    public PagedJournalEntryResponse list(@PathVariable String propertyId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return PagedJournalEntryResponse.from(listJournalEntriesByPropertyUseCase.list(
                new ListJournalEntriesByPropertyQuery(EntityId.of(propertyId), PageRequest.of(page, size))));
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/entries/{entryId}/validation")
    public JournalEntryResponse validate(@PathVariable String propertyId, @PathVariable String entryId) {
        return JournalEntryResponse.from(
                postJournalEntryUseCase.post(new PostJournalEntryCommand(JournalEntryId.of(entryId))));
    }

    private CreateJournalEntryLineCommand toLineCommand(CreateJournalEntryLineRequest request) {
        return new CreateJournalEntryLineCommand(LedgerAccountId.of(request.ledgerAccountId()),
                request.auxiliaryUnitId() == null ? null : EntityId.of(request.auxiliaryUnitId()),
                request.auxiliaryPartyId() == null ? null : EntityId.of(request.auxiliaryPartyId()),
                request.direction(), request.amount(), request.label());
    }
}
