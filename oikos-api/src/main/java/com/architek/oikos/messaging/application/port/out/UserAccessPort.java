package com.architek.oikos.messaging.application.port.out;

import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to answer "which properties is this user a member of"
 * and "can this user act on this property" questions, without messaging
 * depending on user's domain/repository directly (rule 6). Implemented in
 * messaging.infrastructure.adapter.MessagingUserAccessAdapter, delegating to
 * user.application.port.in.GetUserAccessUseCase - the same use case
 * PropertyAccessEvaluator itself already relies on.
 */
public interface UserAccessPort {

    /** Every propertyId (staff or plain owner role) this user currently holds any role on. */
    Set<EntityId> memberPropertyIds(EntityId userId);

    boolean isMember(EntityId userId, EntityId propertyId);

    boolean canBroadcast(EntityId userId, EntityId propertyId);

    /** True for a STAFF property-scoped role (board or manager, admin or member tier) - the population eligible for SenderIdentity.BOARD and for BOARD_PRIVATE membership. */
    boolean managesProperty(EntityId userId, EntityId propertyId);

    /** True if the user holds PROPERTY_OWNER on this property - the population eligible for SenderIdentity.OWNER. */
    boolean ownsProperty(EntityId userId, EntityId propertyId);
}
