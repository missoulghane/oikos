package com.architek.oikos.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class RecipientGroupTest {

    @Test
    void a_group_keeps_its_name_trimmed_and_its_members() {
        EntityId member = EntityId.newId();

        RecipientGroup group = RecipientGroup.create(RecipientGroupId.newId(), EntityId.newId(), "  Bâtiment 1  ",
                Set.of(member), EntityId.newId());

        assertThat(group.getName()).isEqualTo("Bâtiment 1");
        assertThat(group.getMemberUserIds()).containsExactly(member);
    }

    @Test
    void a_group_without_a_name_is_rejected() {
        assertThatThrownBy(() -> RecipientGroup.create(RecipientGroupId.newId(), EntityId.newId(), "   ",
                Set.of(EntityId.newId()), EntityId.newId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Un groupe vide ne sert à rien : composer avec lui n'enverrait à personne.
    @Test
    void a_group_without_a_member_is_rejected() {
        assertThatThrownBy(() -> RecipientGroup.create(RecipientGroupId.newId(), EntityId.newId(), "Bâtiment 1",
                Set.of(), EntityId.newId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updating_keeps_the_identity_and_the_author() {
        EntityId author = EntityId.newId();
        RecipientGroupId id = RecipientGroupId.newId();
        RecipientGroup group = RecipientGroup.create(id, EntityId.newId(), "Bâtiment 1", Set.of(EntityId.newId()), author);

        RecipientGroup updated = group.update("Bâtiment 2", Set.of(EntityId.newId()));

        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getCreatedBy()).isEqualTo(author);
        assertThat(updated.getName()).isEqualTo("Bâtiment 2");
    }
}
