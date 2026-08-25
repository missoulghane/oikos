package com.architek.oikos.invitation.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface InvitationRepository {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(InvitationId id);

    Optional<Invitation> findByToken(String token);

    Page<Invitation> findAllByPropertyId(EntityId propertyId, PageRequest pageRequest);

    /**
     * Used by ListMembershipRequestsService to surface still-outstanding
     * PRIVATE invitations (issued, not yet accepted) in the manager's
     * unified membership-request overview - callers still need to filter
     * the result by Invitation.isUsable(now) themselves, since ACTIVE alone
     * doesn't account for expiry (there's no stored EXPIRED status).
     */
    List<Invitation> findAllByPropertyIdAndTypeAndStatus(EntityId propertyId, InvitationType type, InvitationStatus status);

    /**
     * Le lien public d'une copropriété, quel que soit son statut. Il est
     * unique par copropriété par décision produit : on le désactive et on le
     * réactive (voir EnableInvitationUseCase), on n'en crée jamais un second,
     * pour qu'un QR code déjà imprimé ou affiché ne devienne pas caduc.
     * Optional.empty() tant qu'aucun n'a été créé.
     */
    Optional<Invitation> findPublicByPropertyId(EntityId propertyId);

    /**
     * L'invitation privée encore ouverte adressée à ce contact, s'il y en a
     * une. ACTIVE seulement - l'appelant filtre lui-même l'expiration, comme
     * pour findAllByPropertyIdAndTypeAndStatus : il n'existe pas de statut
     * EXPIRED stocké. Il n'y en a jamais plus d'une ouverte à la fois, une
     * nouvelle invitation fermant la précédente (voir CreateInvitationService).
     */
    Optional<Invitation> findOutstandingPrivateByPartyId(EntityId partyId);
}
