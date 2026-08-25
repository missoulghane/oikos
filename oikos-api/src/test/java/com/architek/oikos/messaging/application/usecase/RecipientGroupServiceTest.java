package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.command.CreateRecipientGroupCommand;
import com.architek.oikos.messaging.application.command.DeleteRecipientGroupCommand;
import com.architek.oikos.messaging.application.command.UpdateRecipientGroupCommand;
import com.architek.oikos.messaging.application.dto.RecipientGroupView;
import com.architek.oikos.messaging.application.query.ListRecipientGroupsQuery;
import com.architek.oikos.messaging.domain.exception.RecipientGroupNotFoundException;
import com.architek.oikos.messaging.domain.exception.RecipientNotPropertyMemberException;
import com.architek.oikos.messaging.domain.model.RecipientGroup;
import com.architek.oikos.messaging.domain.repository.RecipientGroupRepository;
import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecipientGroupServiceTest {

    @Mock
    private RecipientGroupRepository recipientGroupRepository;

    @Mock
    private MemberDisplayNameResolver memberDisplayNameResolver;

    private RecipientGroupService newService() {
        return new RecipientGroupService(recipientGroupRepository, memberDisplayNameResolver);
    }

    @Test
    void creates_a_group_of_members_of_the_property() {
        EntityId propertyId = EntityId.newId();
        EntityId creator = EntityId.newId();
        EntityId memberA = EntityId.newId();
        EntityId memberB = EntityId.newId();
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(memberA, "Alice", memberB, "Bob"));
        when(recipientGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecipientGroupId id = newService().create(new CreateRecipientGroupCommand(propertyId, creator,
                "Habitants du bâtiment 1", Set.of(memberA, memberB)));

        assertThat(id).isNotNull();
        ArgumentCaptor<RecipientGroup> captor = ArgumentCaptor.forClass(RecipientGroup.class);
        verify(recipientGroupRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Habitants du bâtiment 1");
        assertThat(captor.getValue().getMemberUserIds()).containsExactlyInAnyOrder(memberA, memberB);
    }

    // On ne range pas dans un groupe quelqu'un qui n'est pas de la copropriété,
    // ou qui n'a pas de compte pour recevoir - il ne recevrait jamais rien.
    @Test
    void refuses_a_member_who_is_not_a_member_of_the_property() {
        EntityId propertyId = EntityId.newId();
        EntityId outsider = EntityId.newId();
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(EntityId.newId(), "Alice"));

        assertThatThrownBy(() -> newService().create(new CreateRecipientGroupCommand(propertyId, EntityId.newId(),
                "Bâtiment 1", Set.of(outsider))))
                .isInstanceOf(RecipientNotPropertyMemberException.class);
        verify(recipientGroupRepository, never()).save(any());
    }

    @Test
    void renames_and_replaces_the_members_of_an_existing_group() {
        EntityId propertyId = EntityId.newId();
        EntityId memberA = EntityId.newId();
        EntityId memberB = EntityId.newId();
        RecipientGroupId groupId = RecipientGroupId.newId();
        RecipientGroup existing = RecipientGroup.create(groupId, propertyId, "Bâtiment 1", Set.of(memberA), EntityId.newId());
        when(recipientGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(existing));
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(memberA, "Alice", memberB, "Bob"));
        when(recipientGroupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().update(new UpdateRecipientGroupCommand(groupId, propertyId, "Bâtiment 1 - étage 2", Set.of(memberB)));

        ArgumentCaptor<RecipientGroup> captor = ArgumentCaptor.forClass(RecipientGroup.class);
        verify(recipientGroupRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(groupId);
        assertThat(captor.getValue().getName()).isEqualTo("Bâtiment 1 - étage 2");
        assertThat(captor.getValue().getMemberUserIds()).containsExactly(memberB);
    }

    // L'autorisation du contrôleur porte sur la copropriété de l'URL : sans ce
    // contrôle, en changer l'identifiant donnerait accès au groupe d'une autre.
    @Test
    void refuses_to_touch_a_group_belonging_to_another_property() {
        EntityId propertyId = EntityId.newId();
        RecipientGroupId groupId = RecipientGroupId.newId();
        RecipientGroup elsewhere = RecipientGroup.create(groupId, EntityId.newId(), "Bâtiment 1",
                Set.of(EntityId.newId()), EntityId.newId());
        when(recipientGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(elsewhere));

        assertThatThrownBy(() -> newService().delete(new DeleteRecipientGroupCommand(groupId, propertyId)))
                .isInstanceOf(RecipientGroupNotFoundException.class);
        verify(recipientGroupRepository, never()).deleteById(any());
    }

    @Test
    void lists_the_groups_with_their_members_named() {
        EntityId propertyId = EntityId.newId();
        EntityId memberA = EntityId.newId();
        EntityId memberB = EntityId.newId();
        RecipientGroup group = RecipientGroup.create(RecipientGroupId.newId(), propertyId, "Bâtiment 1",
                Set.of(memberA, memberB), EntityId.newId());
        when(recipientGroupRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(group));
        when(memberDisplayNameResolver.namesByUserId(propertyId)).thenReturn(Map.of(memberA, "Zoé", memberB, "Alice"));

        List<RecipientGroupView> views = newService().listGroups(new ListRecipientGroupsQuery(propertyId));

        assertThat(views).hasSize(1);
        assertThat(views.get(0).members()).extracting(member -> member.fullName()).containsExactly("Alice", "Zoé");
    }

    @Test
    void lists_nothing_and_asks_the_directory_for_nothing_when_the_property_has_no_group() {
        EntityId propertyId = EntityId.newId();
        when(recipientGroupRepository.findAllByPropertyId(propertyId)).thenReturn(List.of());

        assertThat(newService().listGroups(new ListRecipientGroupsQuery(propertyId))).isEmpty();
        verify(memberDisplayNameResolver, never()).namesByUserId(any());
    }
}
