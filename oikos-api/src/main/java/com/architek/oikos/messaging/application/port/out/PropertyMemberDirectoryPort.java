package com.architek.oikos.messaging.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve a property's display name and its full
 * member roster (unit owners with a linked account + active board members),
 * without messaging depending on property's domain/repository directly (rule
 * 6). Implemented in
 * messaging.infrastructure.adapter.MessagingPropertyMemberDirectoryAdapter,
 * delegating to property's public port-in use cases only
 * (GetPropertyUseCase, ListContactsByPropertyUseCase,
 * ListBoardMembersByPropertyUseCase) - zero change in the property module
 * itself.
 */
public interface PropertyMemberDirectoryPort {

    String getPropertyName(EntityId propertyId);

    List<PropertyMemberInfo> listMembers(EntityId propertyId);
}
