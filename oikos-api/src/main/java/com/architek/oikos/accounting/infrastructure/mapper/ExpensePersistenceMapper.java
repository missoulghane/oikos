package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.accounting.infrastructure.persistence.ExpenseEntity;
import com.architek.oikos.shared.domain.valueobject.Amount;

@Mapper(componentModel = "spring")
public interface ExpensePersistenceMapper {

    default ExpenseEntity toEntity(Expense expense) {
        return toEntity(expense, new ExpenseEntity());
    }

    default ExpenseEntity toEntity(Expense expense, ExpenseEntity entity) {
        entity.setId(expense.getId().asUuid());
        entity.setExerciseId(expense.getExerciseId().asUuid());
        entity.setFinancialAccountId(expense.getFinancialAccountId().asUuid());
        entity.setDate(expense.getDate());
        entity.setCategory(expense.getCategory());
        entity.setProvider(expense.getProvider());
        entity.setAmount(expense.getAmount().value());
        entity.setDescription(expense.getDescription());
        entity.setReceiptReference(expense.getReceiptReference());
        entity.setJournalEntryId(expense.getJournalEntryId().asUuid());
        return entity;
    }

    default Expense toDomain(ExpenseEntity entity) {
        return Expense.reconstruct(ExpenseId.of(entity.getId()), AccountingExerciseId.of(entity.getExerciseId()),
                FinancialAccountId.of(entity.getFinancialAccountId()), entity.getDate(), entity.getCategory(),
                entity.getProvider(), Amount.of(entity.getAmount()), entity.getDescription(),
                entity.getReceiptReference(), FinancialJournalEntryId.of(entity.getJournalEntryId()));
    }
}
