package com.architek.oikos.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

class UserTest {

    private static User newUser() {
        return User.register(UserId.newId(), EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"));
    }

    @Test
    void register_creates_an_unverified_user_with_default_role() {
        User user = newUser();

        assertThat(user.isVerified()).isFalse();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    void registerByAdmin_creates_an_unverified_but_enabled_user_with_default_role() {
        User user = User.registerByAdmin(UserId.newId(), EmailVO.of("jdoe@oikos.com"), "Jane Doe", HashedPassword.of("hashed"));

        assertThat(user.isVerified()).isFalse();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getEmail()).isEqualTo(EmailVO.of("jdoe@oikos.com"));
        assertThat(user.getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    void deactivate_and_activate_toggle_enabled_and_are_idempotent() {
        User user = newUser();

        User deactivated = user.deactivate();
        assertThat(deactivated.isEnabled()).isFalse();
        assertThat(deactivated.deactivate()).isSameAs(deactivated);

        User reactivated = deactivated.activate();
        assertThat(reactivated.isEnabled()).isTrue();
        assertThat(reactivated.activate()).isSameAs(reactivated);
    }

    @Test
    void verify_returns_a_new_verified_instance_with_same_identity() {
        User user = newUser();

        User verified = user.verify();

        assertThat(verified.isVerified()).isTrue();
        assertThat(verified).isEqualTo(user);
        assertThat(user.isVerified()).isFalse();
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        User a = User.register(UserId.newId(), EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"));
        User b = User.register(a.getId(), EmailVO.of("other@doe.com"), "Other Name", HashedPassword.of("other"));

        assertThat(a).isEqualTo(b);
    }

    @Test
    void withPassword_replaces_only_the_password() {
        User user = newUser();

        User updated = user.withPassword(HashedPassword.of("new-hash"));

        assertThat(updated.getPassword().value()).isEqualTo("new-hash");
        assertThat(updated.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void withEmail_replaces_only_the_email() {
        User user = newUser();

        User updated = user.withEmail(EmailVO.of("new@doe.com"));

        assertThat(updated.getEmail()).isEqualTo(EmailVO.of("new@doe.com"));
        assertThat(updated.getFullName()).isEqualTo(user.getFullName());
    }

    @Test
    void withFullName_replaces_only_the_full_name() {
        User user = newUser();

        User updated = user.withFullName("New Name");

        assertThat(updated.getFullName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void withLinkedParty_adds_a_party_id_to_the_linked_set() {
        User user = newUser();
        EntityId partyId = EntityId.newId();

        User updated = user.withLinkedParty(partyId);

        assertThat(updated.getLinkedPartyIds()).containsExactly(partyId);
    }

    @Test
    void withPropertyRoleGrant_adds_a_grant_to_the_set() {
        User user = newUser();
        EntityId partyId = EntityId.newId();
        EntityId propertyId = EntityId.newId();

        User updated = user.withPropertyRoleGrant(partyId, propertyId, PropertyRole.PROPERTY_BOARD_ADMIN);

        assertThat(updated.getPropertyRoleGrants())
                .containsExactly(new PropertyRoleGrant(partyId, propertyId, PropertyRole.PROPERTY_BOARD_ADMIN));
    }
}
