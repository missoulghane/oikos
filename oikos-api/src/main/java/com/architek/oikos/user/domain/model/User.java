package com.architek.oikos.user.domain.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * User account aggregate: a fully standalone platform account (own email,
 * full name, login credentials, application-level status). Independent of
 * any Party - it may optionally be linked to any number of per-property
 * Party rows ({@link #linkedPartyIds}), each carrying its own set of
 * property-scoped role grants ({@link #propertyRoleGrants}), but needs
 * neither to exist (e.g. a pure platform ROLE_ADMIN/ROLE_MASTER account).
 * The account's own email/phone take precedence over every linked Party's
 * copy - see UpdateUserProfileService, which pushes a change here down to
 * each linked Party rather than the other way around.
 * Immutable: every mutation returns a new instance. Entity semantics: equals/hashCode
 * are identity-based (on id), not value-based.
 */
public final class User {

    private final UserId id;
    private final EmailVO email;
    private final String fullName;
    private final String phone;
    private final HashedPassword password;
    private final Set<Role> roles;
    private final Set<EntityId> linkedPartyIds;
    private final Set<PropertyRoleGrant> propertyRoleGrants;
    private final boolean verified;
    private final boolean enabled;
    private final byte[] avatar;
    private final String avatarContentType;

    private User(UserId id, EmailVO email, String fullName, String phone, HashedPassword password, Set<Role> roles,
                  Set<EntityId> linkedPartyIds, Set<PropertyRoleGrant> propertyRoleGrants,
                  boolean verified, boolean enabled, byte[] avatar, String avatarContentType) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.fullName = requireNonBlank(fullName, "fullName");
        // Normalized to null rather than blank, same rationale as Party.phone.
        this.phone = (phone == null || phone.isBlank()) ? null : phone;
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
        if (this.roles.isEmpty()) {
            throw new IllegalArgumentException("a user must have at least one role");
        }
        this.linkedPartyIds = Set.copyOf(Objects.requireNonNull(linkedPartyIds, "linkedPartyIds must not be null"));
        this.propertyRoleGrants =
                Set.copyOf(Objects.requireNonNull(propertyRoleGrants, "propertyRoleGrants must not be null"));
        this.verified = verified;
        this.enabled = enabled;
        // avatar/avatarContentType always travel together: no content type without bytes,
        // no bytes without a content type - see withAvatar/withoutAvatar.
        this.avatar = avatar == null ? null : avatar.clone();
        this.avatarContentType = avatar == null ? null : avatarContentType;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public static User register(UserId id, EmailVO email, String fullName, HashedPassword password) {
        return register(id, email, fullName, password, Role.ROLE_USER);
    }

    public static User register(UserId id, EmailVO email, String fullName, HashedPassword password, Role role) {
        return new User(id, email, fullName, null, password, Set.of(role), Set.of(), Set.of(), false, true, null, null);
    }

    /**
     * Admin-created accounts still go through activation: the password passed
     * here is a random, server-generated placeholder (never usable, since
     * verified=false already blocks login) until the invited user activates their
     * account and chooses their own password via {@link #verify()}/{@link
     * #withPassword(HashedPassword)}.
     */
    public static User registerByAdmin(UserId id, EmailVO email, String fullName, HashedPassword password) {
        return new User(id, email, fullName, null, password, Set.of(Role.ROLE_USER), Set.of(), Set.of(), false, true,
                null, null);
    }

    public static User reconstruct(UserId id, EmailVO email, String fullName, String phone, HashedPassword password,
                                    Set<Role> roles, Set<EntityId> linkedPartyIds,
                                    Set<PropertyRoleGrant> propertyRoleGrants, boolean verified, boolean enabled,
                                    byte[] avatar, String avatarContentType) {
        return new User(id, email, fullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, avatar, avatarContentType);
    }

    public User verify() {
        if (verified) {
            return this;
        }
        return new User(id, email, fullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, true,
                enabled, avatar, avatarContentType);
    }

    public User withPassword(HashedPassword newPassword) {
        return new User(id, email, fullName, phone, newPassword, roles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, avatar, avatarContentType);
    }

    public User withEmail(EmailVO newEmail) {
        return new User(id, newEmail, fullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, avatar, avatarContentType);
    }

    public User withFullName(String newFullName) {
        return new User(id, email, newFullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, avatar, avatarContentType);
    }

    public User withPhone(String newPhone) {
        return new User(id, email, fullName, newPhone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, avatar, avatarContentType);
    }

    public User withRoles(Set<Role> newRoles) {
        return new User(id, email, fullName, phone, password, newRoles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, avatar, avatarContentType);
    }

    public User withLinkedParty(EntityId partyId) {
        Objects.requireNonNull(partyId, "partyId must not be null");
        Set<EntityId> updated = new HashSet<>(linkedPartyIds);
        updated.add(partyId);
        return new User(id, email, fullName, phone, password, roles, updated, propertyRoleGrants, verified, enabled,
                avatar, avatarContentType);
    }

    public User withPropertyRoleGrant(EntityId partyId, EntityId propertyId, PropertyRole role) {
        Set<PropertyRoleGrant> updated = new HashSet<>(propertyRoleGrants);
        updated.add(new PropertyRoleGrant(partyId, propertyId, role));
        return new User(id, email, fullName, phone, password, roles, linkedPartyIds, updated, verified, enabled,
                avatar, avatarContentType);
    }

    public User withAvatar(byte[] newAvatar, String newAvatarContentType) {
        Objects.requireNonNull(newAvatar, "newAvatar must not be null");
        Objects.requireNonNull(newAvatarContentType, "newAvatarContentType must not be null");
        return new User(id, email, fullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, newAvatar, newAvatarContentType);
    }

    public User withoutAvatar() {
        if (avatar == null) {
            return this;
        }
        return new User(id, email, fullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                enabled, null, null);
    }

    /**
     * Admin-triggered account status, independent of email verification: a
     * disabled account cannot authenticate regardless of {@link #verified}.
     */
    public User activate() {
        if (enabled) {
            return this;
        }
        return new User(id, email, fullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                true, avatar, avatarContentType);
    }

    public User deactivate() {
        if (!enabled) {
            return this;
        }
        return new User(id, email, fullName, phone, password, roles, linkedPartyIds, propertyRoleGrants, verified,
                false, avatar, avatarContentType);
    }

    public UserId getId() {
        return id;
    }

    public EmailVO getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<EntityId> getLinkedPartyIds() {
        return linkedPartyIds;
    }

    public Set<PropertyRoleGrant> getPropertyRoleGrants() {
        return propertyRoleGrants;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public byte[] getAvatar() {
        return avatar == null ? null : avatar.clone();
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public boolean hasAvatar() {
        return avatar != null;
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
