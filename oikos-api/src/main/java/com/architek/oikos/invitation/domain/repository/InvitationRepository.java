package com.architek.oikos.invitation.domain.repository;

import java.util.Optional;

import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface InvitationRepository {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(InvitationId id);

    Optional<Invitation> findByToken(String token);

    Page<Invitation> findAllByPropertyId(EntityId propertyId, PageRequest pageRequest);
}
