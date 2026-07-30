package com.architek.oikos.accounting.domain.repository;

import java.util.Optional;

import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface AccountingExerciseRepository {

    AccountingExercise save(AccountingExercise exercise);

    Optional<AccountingExercise> findById(AccountingExerciseId id);

    Optional<AccountingExercise> findOpenByPropertyId(EntityId propertyId);

    boolean existsOpenByPropertyId(EntityId propertyId);
}
