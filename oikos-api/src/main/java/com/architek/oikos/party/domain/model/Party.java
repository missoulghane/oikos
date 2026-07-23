package com.architek.oikos.party.domain.model;

import java.util.Objects;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Identity record of a legal actor known to the system (individual or
 * company), independent of any application account (see {@code
 * com.architek.oikos.user.domain.model.User}, which references a Party by id
 * rather than duplicating identity fields).
 * Immutable: every mutation returns a new instance. Entity semantics: equals/hashCode
 * are identity-based (on id), not value-based.
 */
public final class Party {

    private final PartyId id;
    private final String fullName;
    private final PartyType partyType;
    private final EmailVO email;
    private final String phone;

    private Party(PartyId id, String fullName, PartyType partyType, EmailVO email, String phone) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.fullName = requireNonBlank(fullName, "fullName");
        this.partyType = Objects.requireNonNull(partyType, "partyType must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.phone = phone;
    }

    public static Party create(PartyId id, String fullName, PartyType partyType, EmailVO email, String phone) {
        return new Party(id, fullName, partyType, email, phone);
    }

    public static Party reconstruct(PartyId id, String fullName, PartyType partyType, EmailVO email, String phone) {
        return new Party(id, fullName, partyType, email, phone);
    }

    public Party withPartyInfo(String newFullName, PartyType newPartyType, EmailVO newEmail, String newPhone) {
        return new Party(id, newFullName, newPartyType, newEmail, newPhone);
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

    public String getFullName() {
        return fullName;
    }

    public PartyType getPartyType() {
        return partyType;
    }

    public EmailVO getEmail() {
        return email;
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
