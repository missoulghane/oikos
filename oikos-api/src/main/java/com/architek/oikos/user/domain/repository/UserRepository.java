package com.architek.oikos.user.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.domain.valueobject.UserSearchCriteria;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByLinkedPartyId(EntityId partyId);

    /**
     * Batch counterpart of {@link #existsByLinkedPartyId(EntityId)}: the subset of
     * {@code partyIds} that already have a linked AppUser account (single query,
     * no N+1), used by list views showing several parties at once.
     */
    Set<EntityId> findLinkedPartyIds(Collection<EntityId> partyIds);

    /**
     * Batch counterpart of {@link #findLinkedPartyIds(Collection)} returning the
     * actual User aggregates rather than just their linked party ids, used by
     * FindUsersByPartyIdsService to resolve partyId -> userId (a user's other,
     * unrelated linkedPartyIds come along for the ride and must be filtered by
     * the caller against the requested batch).
     */
    List<User> findByLinkedPartyIds(Collection<EntityId> partyIds);

    Page<User> findAll(PageRequest pageRequest, UserSearchCriteria criteria);

    void deleteById(UserId id);
}
