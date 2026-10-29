package com.architek.oikos.accounting.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface AccountingExerciseRepository {

    AccountingExercise save(AccountingExercise exercise);

    Optional<AccountingExercise> findById(AccountingExerciseId id);

    /** Every exercise of the property, open or closed, oldest first (by start date). */
    List<AccountingExercise> findAllByPropertyId(EntityId propertyId);

    /**
     * The exercise whose dates cover the given day, whatever its status. At most
     * one: a property's exercises never overlap (enforced on opening, and by an
     * exclusion constraint in the database).
     */
    Optional<AccountingExercise> findCoveringDate(EntityId propertyId, LocalDate date);
}
