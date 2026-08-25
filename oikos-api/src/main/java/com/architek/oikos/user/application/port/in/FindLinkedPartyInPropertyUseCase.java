package com.architek.oikos.user.application.port.in;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * « Quelle fiche contact, dans cette copropriété, appartient au titulaire de
 * cette adresse de connexion ? »
 *
 * <p>L'adresse d'un compte et celle de la fiche contact du même être humain
 * peuvent différer : le syndic enregistre le copropriétaire avec l'adresse
 * qu'il a sous la main, et celui-ci crée ensuite son compte avec une autre
 * (voir SubmitMembershipRequestService, qui prévoit exactement ce cas). Un
 * rapprochement qui ne regarde que les fiches contacts rate donc la personne
 * dès que le syndic tape son adresse de connexion - et fabrique un doublon.
 *
 * <p>Le rattachement passe par les rôles de copropriété du compte
 * (PropertyRoleGrant), seuls porteurs du couple contact/copropriété : le
 * simple lien compte-contact, lui, ne dit pas de quelle copropriété il s'agit.
 * Un compte rattaché à une fiche sans aucun rôle sur la copropriété n'est donc
 * pas retrouvé - un état que seul l'ancien parcours d'invitation de compte
 * produisait, et qui n'est plus émis.
 */
public interface FindLinkedPartyInPropertyUseCase {

    Optional<EntityId> findLinkedParty(EmailVO accountEmail, EntityId propertyId);
}
