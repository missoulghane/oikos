package com.architek.oikos.property.application.port.in;

import java.util.Optional;

import com.architek.oikos.property.application.dto.LinkedContactView;
import com.architek.oikos.property.application.query.FindContactByAccountEmailQuery;

/**
 * Le pendant en lecture du troisième filet de AddBoardMemberService /
 * AddUnitOwnerService : il laisse les écrans de rattachement annoncer le
 * rapprochement avant l'envoi, au lieu de le faire découvrir après coup.
 * Optional.empty() quand aucun compte ne porte cette adresse, ou qu'il n'a
 * pas de fiche dans cette copropriété.
 */
public interface FindContactByAccountEmailUseCase {

    Optional<LinkedContactView> findContact(FindContactByAccountEmailQuery query);
}
