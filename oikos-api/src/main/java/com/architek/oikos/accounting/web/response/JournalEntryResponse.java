package com.architek.oikos.accounting.web.response;

import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;

public record JournalEntryResponse(String id, String propertyId, String exerciseId, String periodId,
                                    JournalCode journalCode, String treasuryAccountId, LocalDate pieceDate,
                                    Integer pieceNumber, String externalReference, JournalEntryStatus status,
                                    String originalEntryId, String createdByUserId,
                                    List<JournalEntryLineResponse> lines) {

    public static JournalEntryResponse from(JournalEntryView view) {
        return new JournalEntryResponse(view.id().toString(), view.propertyId().toString(), view.exerciseId().toString(),
                view.periodId().toString(), view.journalCode(),
                view.treasuryAccountId() == null ? null : view.treasuryAccountId().toString(), view.pieceDate(),
                view.pieceNumber(), view.externalReference(), view.status(),
                view.originalEntryId() == null ? null : view.originalEntryId().toString(),
                view.createdByUserId().toString(), view.lines().stream().map(JournalEntryLineResponse::from).toList());
    }
}
