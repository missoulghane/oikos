package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.BoardStaffDirectoryPort;
import com.architek.oikos.property.application.dto.BoardMemberView;
import com.architek.oikos.property.application.port.in.ListBoardMembersByPropertyUseCase;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindUsersByPartyIdsUseCase;

/**
 * Cross-feature adapter: delegates exclusively to property's and user's
 * public port-in use cases (ListBoardMembersByPropertyUseCase filtered to
 * ACTIVE seats, FindUsersByPartyIdsUseCase to turn a party into a platform
 * account), never to their repositories directly (rule 6) - mirrors
 * messaging's MessagingPropertyMemberDirectoryAdapter / MessagingPartyAccountDirectoryAdapter
 * board-loop exactly.
 *
 * <p>Par défaut, tous les rôles du bureau sont prévenus d'une demande
 * d'adhésion : une demande qui n'atteint qu'un seul destinataire attend son
 * retour de vacances. La restriction existe quand même, en configuration
 * ({@code oikos.invitation.notified-board-roles}), parce que certaines
 * copropriétés voudront réserver ces alertes au président et au syndic
 * professionnel plutôt que de les envoyer aux six membres du conseil. Vide =
 * tous les rôles, et c'est le défaut.
 */
@Component
public class InvitationBoardStaffDirectoryAdapter implements BoardStaffDirectoryPort {

    private final ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase;
    private final FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase;
    private final Set<BoardRole> notifiedRoles;

    public InvitationBoardStaffDirectoryAdapter(ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase,
                                                 FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase,
                                                 @Value("${oikos.invitation.notified-board-roles:}") String notifiedBoardRoles) {
        this.listBoardMembersByPropertyUseCase = listBoardMembersByPropertyUseCase;
        this.findUsersByPartyIdsUseCase = findUsersByPartyIdsUseCase;
        this.notifiedRoles = parseRoles(notifiedBoardRoles);
    }

    /**
     * Un nom de rôle inconnu fait échouer le démarrage plutôt que de rétrécir
     * silencieusement la liste des destinataires : une faute de frappe dans une
     * variable d'environnement ne doit pas se solder par des demandes que
     * personne ne reçoit, découvertes des semaines plus tard.
     */
    private static Set<BoardRole> parseRoles(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(BoardRole::valueOf)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    @Override
    public List<EntityId> listStaffUserIds(EntityId propertyId) {
        PropertyId typedPropertyId = PropertyId.of(propertyId.value());
        List<EntityId> activeBoardPartyIds = listBoardMembersByPropertyUseCase
                .listBoardMembers(new ListBoardMembersByPropertyQuery(typedPropertyId)).stream()
                .filter(member -> member.status() == BoardMemberStatus.ACTIVE)
                .filter(member -> notifiedRoles.isEmpty() || notifiedRoles.contains(member.boardRole()))
                .map(BoardMemberView::partyId)
                .toList();
        Map<EntityId, EntityId> userIdsByPartyId = findUsersByPartyIdsUseCase.findUserIdsByPartyIds(activeBoardPartyIds);
        return List.copyOf(userIdsByPartyId.values());
    }
}
