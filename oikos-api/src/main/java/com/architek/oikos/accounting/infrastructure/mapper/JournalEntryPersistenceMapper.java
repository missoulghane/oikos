package com.architek.oikos.accounting.infrastructure.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.infrastructure.persistence.JournalEntryEntity;
import com.architek.oikos.accounting.infrastructure.persistence.JournalEntryLineEntity;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Lines are only ever written once (I4) - newEntity() builds the full entity
 * graph for the first save; updateScalarFields() is used for every
 * subsequent save (a DRAFT-&gt;POSTED or POSTED-&gt;REVERSED transition) and
 * never touches the (already-persisted) lines collection.
 */
@Mapper(componentModel = "spring")
public interface JournalEntryPersistenceMapper {

    default JournalEntryEntity newEntity(JournalEntry entry) {
        JournalEntryEntity entity = new JournalEntryEntity();
        updateScalarFields(entry, entity);
        List<JournalEntryLine> domainLines = entry.getLines();
        List<JournalEntryLineEntity> lineEntities = IntStream.range(0, domainLines.size()).mapToObj(index -> {
            JournalEntryLine line = domainLines.get(index);
            JournalEntryLineEntity lineEntity = new JournalEntryLineEntity();
            lineEntity.setId(line.getId().asUuid());
            lineEntity.setJournalEntry(entity);
            lineEntity.setLedgerAccountId(line.getLedgerAccountId().asUuid());
            lineEntity.setAuxiliaryUnitId(line.getAuxiliaryUnitId().map(EntityId::value).orElse(null));
            lineEntity.setAuxiliaryPartyId(line.getAuxiliaryPartyId().map(EntityId::value).orElse(null));
            lineEntity.setDirection(line.getDirection().name());
            lineEntity.setAmount(line.getAmount().value());
            lineEntity.setLabel(line.getLabel());
            // Preserves display order (see JournalEntryLineEntity.lineOrder) - the
            // domain list's own order is the only place this is tracked upstream.
            lineEntity.setLineOrder(index);
            return lineEntity;
        }).toList();
        entity.setLines(lineEntities);
        return entity;
    }

    default void updateScalarFields(JournalEntry entry, JournalEntryEntity entity) {
        entity.setId(entry.getId().asUuid());
        entity.setPropertyId(entry.getPropertyId().value());
        entity.setExerciseId(entry.getExerciseId().asUuid());
        entity.setPeriodId(entry.getPeriodId().asUuid());
        entity.setJournalCode(entry.getJournalCode().name());
        entity.setTreasuryAccountId(entry.getTreasuryAccountId().map(LedgerAccountId::asUuid).orElse(null));
        entity.setPieceDate(entry.getPieceDate());
        entity.setPieceNumber(entry.getPieceNumber().orElse(null));
        entity.setExternalReference(entry.getExternalReference().orElse(null));
        entity.setStatus(entry.getStatus().name());
        entity.setOriginalEntryId(entry.getOriginalEntryId().map(JournalEntryId::asUuid).orElse(null));
        entity.setCreatedByUserId(entry.getCreatedByUserId().value());
    }

    default JournalEntry toDomain(JournalEntryEntity entity) {
        List<JournalEntryLine> lines = entity.getLines().stream()
                .map(this::toDomainLine)
                .toList();
        UUID treasuryAccountId = entity.getTreasuryAccountId();
        UUID originalEntryId = entity.getOriginalEntryId();
        return JournalEntry.reconstruct(JournalEntryId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                AccountingExerciseId.of(entity.getExerciseId()), PeriodId.of(entity.getPeriodId()),
                JournalCode.valueOf(entity.getJournalCode()), treasuryAccountId == null ? null : LedgerAccountId.of(treasuryAccountId),
                entity.getPieceDate(), entity.getPieceNumber(), entity.getExternalReference(),
                JournalEntryStatus.valueOf(entity.getStatus()), originalEntryId == null ? null : JournalEntryId.of(originalEntryId),
                EntityId.of(entity.getCreatedByUserId()), lines);
    }

    private JournalEntryLine toDomainLine(JournalEntryLineEntity entity) {
        UUID auxiliaryUnitId = entity.getAuxiliaryUnitId();
        UUID auxiliaryPartyId = entity.getAuxiliaryPartyId();
        return JournalEntryLine.of(JournalEntryLineId.of(entity.getId()), LedgerAccountId.of(entity.getLedgerAccountId()),
                auxiliaryUnitId == null ? null : EntityId.of(auxiliaryUnitId),
                auxiliaryPartyId == null ? null : EntityId.of(auxiliaryPartyId),
                EntryDirection.valueOf(entity.getDirection()), Amount.of(entity.getAmount()), entity.getLabel());
    }
}
