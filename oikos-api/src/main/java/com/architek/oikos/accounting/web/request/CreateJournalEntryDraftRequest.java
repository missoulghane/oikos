package com.architek.oikos.accounting.web.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.accounting.domain.valueobject.JournalCode;

public record CreateJournalEntryDraftRequest(@NotNull JournalCode journalCode, String treasuryAccountId,
                                              @NotNull LocalDate pieceDate,
                                              @Size(max = 200) String externalReference,
                                              @NotEmpty @Valid List<CreateJournalEntryLineRequest> lines) {
}
