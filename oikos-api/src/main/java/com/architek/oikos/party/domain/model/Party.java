package com.architek.oikos.party.domain.model;

import java.util.Objects;
import java.util.Optional;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Identity record of a legal actor known to the system (individual or
 * company), scoped to exactly one property (tenant boundary) - the same
 * real person owning units or sitting on boards in two different properties
 * is represented by two distinct Party rows. Independent of any application
 * account (see {@code com.architek.oikos.user.domain.model.User}, which
 * references a Party by id rather than duplicating identity fields).
 * {@code propertyId} is the generic {@link EntityId} rather than the
 * property feature's own PropertyId type, to keep party decoupled from
 * property (same rationale as {@code User.partyId}).
 * Immutable: every mutation returns a new instance. Entity semantics: equals/hashCode
 * are identity-based (on id), not value-based.
 */
public final class Party {

    private final PartyId id;
    private final EntityId propertyId;
    private final String fullName;
    private final PartyType partyType;
    private final EmailVO email;
    private final String phone;

    private Party(PartyId id, EntityId propertyId, String fullName, PartyType partyType, EmailVO email, String phone) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.fullName = requireNonBlank(fullName, "fullName");
        this.partyType = Objects.requireNonNull(partyType, "partyType must not be null");
        // L'email est facultatif depuis qu'un contact peut n'avoir qu'un
        // téléphone : un membre du conseil syndical âgé, un copropriétaire qui
        // n'utilise que son mobile. Il reste unique par copropriété quand il est
        // là - uk_party_property_email s'appuie sur SQL NULL <> NULL, comme le
        // téléphone ci-dessous, pour laisser cohabiter plusieurs contacts sans.
        this.email = email;
        // Normalized to null rather than blank: uk_party_property_phone (property_id, phone)
        // relies on SQL NULL <> NULL to let several parties in the same property have no
        // phone - a blank string would collide with itself under that constraint.
        this.phone = (phone == null || phone.isBlank()) ? null : phone;
    }

    public static Party create(PartyId id, EntityId propertyId, String fullName, PartyType partyType, EmailVO email,
                                String phone) {
        return new Party(id, propertyId, fullName, partyType, email, phone);
    }

    public static Party reconstruct(PartyId id, EntityId propertyId, String fullName, PartyType partyType,
                                     EmailVO email, String phone) {
        return new Party(id, propertyId, fullName, partyType, email, phone);
    }

    public Party withPartyInfo(String newFullName, PartyType newPartyType, EmailVO newEmail, String newPhone) {
        return new Party(id, propertyId, newFullName, newPartyType, newEmail, newPhone);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public PartyId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public String getFullName() {
        return fullName;
    }

    public PartyType getPartyType() {
        return partyType;
    }

    public Optional<EmailVO> getEmail() {
        return Optional.ofNullable(email);
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Party other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
