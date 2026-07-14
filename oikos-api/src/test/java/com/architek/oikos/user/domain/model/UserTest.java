package com.architek.oikos.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.valueobject.UserId;

class UserTest {

    private static User newUser() {
        return User.register(UserId.newId(), EntityId.newId(), HashedPassword.of("hashed"), null);
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
        User user = User.registerByAdmin(UserId.newId(), EntityId.newId(), HashedPassword.of("hashed"), "jdoe");

        assertThat(user.isVerified()).isFalse();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getLogin()).isEqualTo("jdoe");
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
        User a = User.register(UserId.newId(), EntityId.newId(), HashedPassword.of("hashed"), null);
        User b = User.register(a.getId(), EntityId.newId(), HashedPassword.of("other"), "other-login");

        assertThat(a).isEqualTo(b);
    }

    @Test
    void withPassword_replaces_only_the_password() {
        User user = newUser();

        User updated = user.withPassword(HashedPassword.of("new-hash"));

        assertThat(updated.getPassword().value()).isEqualTo("new-hash");
        assertThat(updated.getContactId()).isEqualTo(user.getContactId());
    }

    @Test
    void withLogin_replaces_only_the_login() {
        User user = newUser();

        User updated = user.withLogin("new-login");

        assertThat(updated.getLogin()).isEqualTo("new-login");
        assertThat(updated.getContactId()).isEqualTo(user.getContactId());
    }
}
