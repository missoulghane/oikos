package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface MovementRepository {

    Movement save(Movement movement);

    Optional<Movement> findById(MovementId id);

    /**
     * Unpaginated: used for balance computation and FIFO auto-allocation, both
     * of which need the full ledger of an account, not a page of it.
     */
    List<Movement> findAllByAccountId(AccountId accountId);

    Page<Movement> findPageByAccountId(AccountId accountId, PageRequest pageRequest);
}
