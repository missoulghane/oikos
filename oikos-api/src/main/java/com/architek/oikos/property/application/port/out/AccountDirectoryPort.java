package com.architek.oikos.property.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to reconnaître une personne à son adresse de connexion,
 * quand sa fiche contact en porte une autre. Implemented in
 * property.infrastructure.adapter by delegating to user's public port-in
 * (FindLinkedPartyInPropertyUseCase), never to user's repository directly
 * (rule 6). Distinct de AccountRoleGrantPort, qui écrit un rôle : celui-ci ne
 * fait que lire une identité.
 */
public interface AccountDirectoryPort {

    Optional<EntityId> findLinkedPartyInProperty(EmailVO accountEmail, EntityId propertyId);
}
