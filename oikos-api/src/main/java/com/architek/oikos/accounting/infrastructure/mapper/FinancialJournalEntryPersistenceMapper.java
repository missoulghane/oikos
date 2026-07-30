package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.accounting.infrastructure.persistence.FinancialJournalEntryEntity;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface FinancialJournalEntryPersistenceMapper {

    default FinancialJournalEntryEntity toEntity(FinancialJournalEntry entry) {
        return toEntity(entry, new FinancialJournalEntryEntity());
    }

    default FinancialJournalEntryEntity toEntity(FinancialJournalEntry entry, FinancialJournalEntryEntity entity) {
        entity.setId(entry.getId().asUuid());
        entity.setExerciseId(entry.getExerciseId().asUuid());
        entity.setFinancialAccountId(entry.getFinancialAccountId().asUuid());
        entity.setDate(entry.getDate());
        entity.setType(entry.getType());
        entity.setDirection(entry.getDirection());
        entity.setAmount(entry.getAmount().value());
        entity.setLabel(entry.getLabel());
        entity.setBusinessReference(entry.getBusinessReference());
        entity.setCreatedByUserId(entry.getCreatedByUserId().value());
        return entity;
    }

    default FinancialJournalEntry toDomain(FinancialJournalEntryEntity entity) {
        return FinancialJournalEntry.reconstruct(FinancialJournalEntryId.of(entity.getId()),
                AccountingExerciseId.of(entity.getExerciseId()), FinancialAccountId.of(entity.getFinancialAccountId()),
                entity.getDate(), entity.getType(), entity.getDirection(), Amount.of(entity.getAmount()),
                entity.getLabel(), entity.getBusinessReference(), EntityId.of(entity.getCreatedByUserId()));
    }
}
