package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.infrastructure.persistence.ExpenseEntity;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface ExpensePersistenceMapper {

    default ExpenseEntity toEntity(Expense expense) {
        return toEntity(expense, new ExpenseEntity());
    }

    default ExpenseEntity toEntity(Expense expense, ExpenseEntity entity) {
        entity.setId(expense.getId().asUuid());
        entity.setPropertyId(expense.getPropertyId().value());
        entity.setDate(expense.getDate());
        entity.setLedgerAccountId(expense.getLedgerAccountId().asUuid());
        entity.setAmount(expense.getAmount().value());
        entity.setDescription(expense.getDescription().orElse(null));
        entity.setReceiptReference(expense.getReceiptReference().orElse(null));
        entity.setJournalEntryId(expense.getJournalEntryId().asUuid());
        return entity;
    }

    default Expense toDomain(ExpenseEntity entity) {
        return Expense.reconstruct(ExpenseId.of(entity.getId()), EntityId.of(entity.getPropertyId()), entity.getDate(),
                LedgerAccountId.of(entity.getLedgerAccountId()), Amount.of(entity.getAmount()),
                entity.getDescription(), entity.getReceiptReference(), JournalEntryId.of(entity.getJournalEntryId()));
    }
}
