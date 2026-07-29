package com.architek.oikos.user.infrastructure.persistence;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    private Set<String> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_party", joinColumns = @JoinColumn(name = "app_user_id"))
    @Column(name = "party_id", nullable = false)
    private Set<UUID> linkedPartyIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_party_role", joinColumns = @JoinColumn(name = "app_user_id"))
    private Set<PropertyRoleGrantEmbeddable> propertyRoleGrants = new HashSet<>();

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean enabled;
}
