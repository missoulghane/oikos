package com.architek.oikos.contact.domain.model;

import java.util.Objects;

import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Identity record of a physical person known to the system, independent of any
 * application account (see {@code com.architek.oikos.user.domain.model.User}, which
 * references a Contact by id rather than duplicating identity fields).
 * Immutable: every mutation returns a new instance. Entity semantics: equals/hashCode
 * are identity-based (on id), not value-based.
 */
public final class Contact {

    private final ContactId id;
    private final String lastName;
    private final String firstName;
    private final EmailVO email;
    private final String phone;

    private Contact(ContactId id, String lastName, String firstName, EmailVO email, String phone) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.lastName = requireNonBlank(lastName, "lastName");
        this.firstName = requireNonBlank(firstName, "firstName");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.phone = phone;
    }

    public static Contact create(ContactId id, String lastName, String firstName, EmailVO email, String phone) {
        return new Contact(id, lastName, firstName, email, phone);
    }

    public static Contact reconstruct(ContactId id, String lastName, String firstName, EmailVO email, String phone) {
        return new Contact(id, lastName, firstName, email, phone);
    }

    public Contact withContactInfo(String newLastName, String newFirstName, EmailVO newEmail, String newPhone) {
        return new Contact(id, newLastName, newFirstName, newEmail, newPhone);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public ContactId getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
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
        return o instanceof Contact other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
