package com.architek.oikos.user.domain.model;

/**
 * Closed catalog of atomic, checkable actions. Java call sites reference a
 * specific constant by name (e.g. PropertyAccessEvaluator.hasPermission),
 * so adding a brand-new permission is a code change - only which
 * permissions a given role bundles together is data-driven (see the
 * role_permission table, seeded in V2__rbac_permissions.sql): narrowing a
 * role's bundle, or having a future role reuse an existing permission, is a
 * pure data change.
 */
public enum Permission {
    PROPERTY_READ("property:read"),
    PROPERTY_CREATE("property:create"),
    PROPERTY_UPDATE("property:update"),
    PROPERTY_BOARD_MANAGE("property:board:manage"),
    PROPERTY_MEMBER_INVITE("property:member:invite"),
    UNIT_READ("unit:read"),
    UNIT_WRITE("unit:write"),
    UNIT_OWNERSHIP_WRITE("unit:ownership:write"),
    PARTY_READ("party:read"),
    PARTY_WRITE("party:write"),
    PARTY_INVITE("party:invite"),
    INSTALLMENT_READ("installment:read"),
    INSTALLMENT_CALL_WRITE("installment:call:write"),
    ACCOUNTING_READ("property:accounting:read"),
    ACCOUNTING_WRITE("property:accounting:write"),
    USER_ADMIN("user:admin"),
    INVITATION_MANAGE("invitation:manage");

    private final String key;

    Permission(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static Permission fromKey(String key) {
        for (Permission permission : values()) {
            if (permission.key.equals(key)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission key: " + key);
    }
}
