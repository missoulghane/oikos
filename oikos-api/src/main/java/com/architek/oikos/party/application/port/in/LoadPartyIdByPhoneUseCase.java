package com.architek.oikos.party.application.port.in;

import java.util.Optional;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Pendant téléphonique de {@link LoadPartyIdByEmailUseCase}, pour le même flux
 * « retrouver ou créer le propriétaire » : un syndic connaît souvent le numéro
 * d'un copropriétaire avant son email, et créer un second contact pour la même
 * personne se paie plus tard en doublons dans les convocations et les appels de
 * charges.
 *
 * <p>Le numéro est comparé tel qu'il est stocké, c'est-à-dire au format
 * international composé par le formulaire ; la contrainte
 * uk_party_property_phone garantit qu'il en désigne au plus un par copropriété.
 */
public interface LoadPartyIdByPhoneUseCase {

    Optional<PartyId> loadByPhone(EntityId propertyId, String phone);
}
