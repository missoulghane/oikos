package com.architek.oikos.installment.application.port.out;

import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.installment.application.dto.FundCallLine;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used by GenerateInstallmentCallUseCase to post the VT
 * journal entry for a fund call (P1, spec &sect;6) once the ventilation is
 * known. Implemented in installment.infrastructure.adapter by delegating to
 * accounting's public port-in (PostFundCallJournalEntryUseCase), never to
 * accounting's repository/domain model directly (rule 4/6). Returns the
 * generic EntityId of the resulting JournalEntry, never accounting's own
 * JournalEntryId.
 */
public interface FundCallJournalEntryPort {

    EntityId postFundCallEntry(EntityId propertyId, LocalDate pieceDate, String externalReference,
                               EntityId createdByUserId, List<FundCallLine> lines);
}
