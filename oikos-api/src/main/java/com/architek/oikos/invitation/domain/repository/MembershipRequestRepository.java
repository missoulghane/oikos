package com.architek.oikos.invitation.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface MembershipRequestRepository {

    MembershipRequest save(MembershipRequest membershipRequest);

    Optional<MembershipRequest> findById(MembershipRequestId id);

    Page<MembershipRequest> findAllByPropertyId(EntityId propertyId, PageRequest pageRequest);

    /**
     * Every still-PENDING request targeting this unit, used to auto-reject
     * the siblings of whichever one a manager just accepted.
     */
    List<MembershipRequest> findAllPendingByUnitId(EntityId unitId);
}
