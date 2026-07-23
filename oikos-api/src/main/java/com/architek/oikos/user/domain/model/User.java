package com.architek.oikos.user.domain.model;

import java.util.Objects;
import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * User account aggregate: login credentials and application-level status only.
 * Identity fields (name, email, phone) live on the party feature's Party
 * aggregate, referenced here by id - see {@link #partyId}. The generic
 * {@link EntityId} keeps user fully decoupled from the party feature's own
 * PartyId type (same rationale as auth.domain.model.RefreshToken referencing
 * user by EntityId rather than UserId).
 * Immutable: every mutation returns a new instance. Entity semantics: equals/hashCode
 * are identity-based (on id), not value-based.
 */
public final class User {

    private final UserId id;
    private final EntityId partyId;
    private final HashedPassword password;
    private final String login;
    private final Set<Role> roles;
    private final boolean verified;
    private final boolean enabled;

    private User(UserId id, EntityId partyId, HashedPassword password, String login, Set<Role> roles,
                  boolean verified, boolean enabled) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.partyId = Objects.requireNonNull(partyId, "partyId must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.login = login;
        this.roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
        if (this.roles.isEmpty()) {
            throw new IllegalArgumentException("a user must have at least one role");
        }
        this.verified = verified;
        this.enabled = enabled;
    }

    public static User register(UserId id, EntityId partyId, HashedPassword password, String login) {
        return register(id, partyId, password, login, Role.ROLE_USER);
    }

    public static User register(UserId id, EntityId partyId, HashedPassword password, String login, Role role) {
        return new User(id, partyId, password, login, Set.of(role), false, true);
    }

    /**
     * Admin-created accounts still go through activation: the password passed
     * here is a random, server-generated placeholder (never usable, since
     * verified=false already blocks login) until the invited user activates their
     * account and chooses their own password via {@link #verify()}/{@link
     * #withPassword(HashedPassword)}.
     */
    public static User registerByAdmin(UserId id, EntityId partyId, HashedPassword password, String login) {
        return new User(id, partyId, password, login, Set.of(Role.ROLE_USER), false, true);
    }

    public static User reconstruct(UserId id, EntityId partyId, HashedPassword password, String login,
                                    Set<Role> roles, boolean verified, boolean enabled) {
        return new User(id, partyId, password, login, roles, verified, enabled);
    }

    public User verify() {
        if (verified) {
            return this;
        }
        return new User(id, partyId, password, login, roles, true, enabled);
    }

    public User withPassword(HashedPassword newPassword) {
        return new User(id, partyId, newPassword, login, roles, verified, enabled);
    }

    public User withLogin(String newLogin) {
        return new User(id, partyId, password, newLogin, roles, verified, enabled);
    }

    public User withRoles(Set<Role> newRoles) {
        return new User(id, partyId, password, login, newRoles, verified, enabled);
    }

    /**
     * Admin-triggered account status, independent of email verification: a
     * disabled account cannot authenticate regardless of {@link #verified}.
     */
    public User activate() {
        if (enabled) {
            return this;
        }
        return new User(id, partyId, password, login, roles, verified, true);
    }

    public User deactivate() {
        if (!enabled) {
            return this;
        }
        return new User(id, partyId, password, login, roles, verified, false);
    }

    public UserId getId() {
        return id;
    }

    public EntityId getPartyId() {
        return partyId;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof User other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
