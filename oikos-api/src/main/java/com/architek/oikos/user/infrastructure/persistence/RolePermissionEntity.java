package com.architek.oikos.user.infrastructure.persistence;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Read-only projection of a role_permission row (role_name, permission_key).
 * Rows are seeded/edited in the Flyway migration, never written through JPA.
 */
@Entity
@Table(name = "role_permission")
@IdClass(RolePermissionEntity.RolePermissionKey.class)
public class RolePermissionEntity {

    @Id
    @Column(name = "role_name")
    private String roleName;

    @Id
    @Column(name = "permission_key")
    private String permissionKey;

    protected RolePermissionEntity() {
    }

    public String getRoleName() {
        return roleName;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public static class RolePermissionKey implements Serializable {
        private String roleName;
        private String permissionKey;

        public RolePermissionKey() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof RolePermissionKey other
                    && Objects.equals(roleName, other.roleName)
                    && Objects.equals(permissionKey, other.permissionKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleName, permissionKey);
        }
    }
}
