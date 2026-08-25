package com.architek.oikos.messaging.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.CreateRecipientGroupCommand;
import com.architek.oikos.messaging.application.command.DeleteRecipientGroupCommand;
import com.architek.oikos.messaging.application.command.UpdateRecipientGroupCommand;
import com.architek.oikos.messaging.application.dto.ConversationParticipantView;
import com.architek.oikos.messaging.application.dto.RecipientGroupView;
import com.architek.oikos.messaging.application.port.in.CreateRecipientGroupUseCase;
import com.architek.oikos.messaging.application.port.in.DeleteRecipientGroupUseCase;
import com.architek.oikos.messaging.application.port.in.ListRecipientGroupsUseCase;
import com.architek.oikos.messaging.application.port.in.UpdateRecipientGroupUseCase;
import com.architek.oikos.messaging.application.query.ListRecipientGroupsQuery;
import com.architek.oikos.messaging.domain.exception.RecipientGroupNotFoundException;
import com.architek.oikos.messaging.domain.exception.RecipientNotPropertyMemberException;
import com.architek.oikos.messaging.domain.model.RecipientGroup;
import com.architek.oikos.messaging.domain.repository.RecipientGroupRepository;
import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Les quatre gestes sur un carnet d'adresses, réunis : ils partagent la même
 * vérification (le groupe appartient bien à la copropriété de l'URL) et la même
 * résolution de noms, et les séparer en quatre classes n'aurait recopié que ça.
 *
 * <p>L'autorisation vit en amont, sur le contrôleur : créer, modifier et
 * supprimer sont réservés au bureau (managesProperty, bénévole comme pro),
 * lister est ouvert à qui peut écrire sur la copropriété - un groupe ne sert à
 * rien si on ne peut pas le choisir en composant.
 *
 * <p>Les membres sont vérifiés contre l'annuaire de la copropriété : on ne
 * range pas dans un groupe quelqu'un qui n'en fait pas partie, ou qui n'a pas
 * de compte pour recevoir (même règle que StartGroupConversationService).
 */
@Component
public class RecipientGroupService implements CreateRecipientGroupUseCase, UpdateRecipientGroupUseCase,
        DeleteRecipientGroupUseCase, ListRecipientGroupsUseCase {

    private final RecipientGroupRepository recipientGroupRepository;
    private final MemberDisplayNameResolver memberDisplayNameResolver;

    public RecipientGroupService(RecipientGroupRepository recipientGroupRepository,
                                  MemberDisplayNameResolver memberDisplayNameResolver) {
        this.recipientGroupRepository = recipientGroupRepository;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
    }

    @Override
    @Transactional
    public RecipientGroupId create(CreateRecipientGroupCommand command) {
        requireMembersOfProperty(command.propertyId(), command.memberUserIds());
        RecipientGroup group = RecipientGroup.create(RecipientGroupId.newId(), command.propertyId(), command.name(),
                command.memberUserIds(), command.createdBy());
        return recipientGroupRepository.save(group).getId();
    }

    @Override
    @Transactional
    public void update(UpdateRecipientGroupCommand command) {
        RecipientGroup group = requireGroupOfProperty(command.groupId(), command.propertyId());
        requireMembersOfProperty(command.propertyId(), command.memberUserIds());
        recipientGroupRepository.save(group.update(command.name(), command.memberUserIds()));
    }

    @Override
    @Transactional
    public void delete(DeleteRecipientGroupCommand command) {
        requireGroupOfProperty(command.groupId(), command.propertyId());
        recipientGroupRepository.deleteById(command.groupId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipientGroupView> listGroups(ListRecipientGroupsQuery query) {
        List<RecipientGroup> groups = recipientGroupRepository.findAllByPropertyId(query.propertyId());
        if (groups.isEmpty()) {
            return List.of();
        }
        // Un seul croisement de l'annuaire pour toute la liste.
        Map<EntityId, String> namesByUserId = memberDisplayNameResolver.namesByUserId(query.propertyId());

        return groups.stream().map(group -> new RecipientGroupView(group.getId(), group.getPropertyId(), group.getName(),
                        group.getMemberUserIds().stream()
                                .map(userId -> new ConversationParticipantView(userId, namesByUserId.get(userId)))
                                .sorted(Comparator.comparing(ConversationParticipantView::fullName,
                                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                                .toList()))
                .toList();
    }

    /**
     * Le groupe existe et appartient bien à la copropriété de l'URL. Sans ce
     * contrôle, l'autorisation portée par le contrôleur (« je gère cette
     * copropriété ») laisserait modifier le groupe d'une autre en changeant
     * simplement l'identifiant.
     */
    private RecipientGroup requireGroupOfProperty(RecipientGroupId groupId, EntityId propertyId) {
        RecipientGroup group = recipientGroupRepository.findById(groupId)
                .orElseThrow(() -> new RecipientGroupNotFoundException(groupId));
        if (!group.getPropertyId().equals(propertyId)) {
            throw new RecipientGroupNotFoundException(groupId);
        }
        return group;
    }

    private void requireMembersOfProperty(EntityId propertyId, Set<EntityId> memberUserIds) {
        Set<EntityId> eligible = memberDisplayNameResolver.namesByUserId(propertyId).keySet();
        memberUserIds.stream()
                .filter(userId -> !eligible.contains(userId))
                .findFirst()
                .ifPresent(userId -> {
                    throw new RecipientNotPropertyMemberException(
                            "user " + userId + " is not a member with an account of property " + propertyId);
                });
    }
}
