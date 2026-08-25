package com.architek.oikos.invitation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;

public interface InvitationJpaRepository extends JpaRepository<InvitationEntity, UUID> {

    Optional<InvitationEntity> findByToken(String token);

    Page<InvitationEntity> findByPropertyId(UUID propertyId, Pageable pageable);

    List<InvitationEntity> findByPropertyIdAndTypeAndStatus(UUID propertyId, InvitationType type, InvitationStatus status);

    /**
     * Trié par date de création : rien n'empêche techniquement deux lignes
     * PUBLIC pour une même copropriété (les bases d'avant cette règle en ont),
     * et le lien historique - celui dont le QR code circule - est le premier.
     */
    List<InvitationEntity> findByPropertyIdAndTypeOrderByCreatedDateAsc(UUID propertyId, InvitationType type);

    /**
     * Trié du plus récent au plus ancien : la règle « une seule ouverte à la
     * fois » est appliquée à l'écriture, ce tri n'est que la ceinture qui va
     * avec les bretelles pour les lignes antérieures.
     */
    List<InvitationEntity> findByTargetPartyIdAndStatusOrderByCreatedDateDesc(UUID targetPartyId, InvitationStatus status);
}
