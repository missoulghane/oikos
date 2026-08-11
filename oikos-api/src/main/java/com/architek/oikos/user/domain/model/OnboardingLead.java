package com.architek.oikos.user.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.user.domain.valueobject.OnboardingLeadId;

/**
 * Email captured on step 1 of the volunteer-syndic wizard, before any account
 * exists (an account cannot be created before step 2, see V20). Purely an
 * acquisition record: it grants nothing and is never used for authentication.
 * convertedAt is set once the same address completes registration, so a
 * follow-up only ever targets leads that never became accounts.
 * Immutable: converting returns a new instance.
 */
public record OnboardingLead(OnboardingLeadId id, EmailVO email, String firstName, String lastName,
                              Instant convertedAt) {

    public OnboardingLead {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(email, "email must not be null");
    }

    public static OnboardingLead capture(EmailVO email, String firstName, String lastName) {
        return new OnboardingLead(OnboardingLeadId.newId(), email, firstName, lastName, null);
    }

    /** Re-typing the same address updates the names rather than creating a second lead. */
    public OnboardingLead withNames(String newFirstName, String newLastName) {
        return new OnboardingLead(id, email, newFirstName, newLastName, convertedAt);
    }

    public OnboardingLead convertedAt(Instant instant) {
        return new OnboardingLead(id, email, firstName, lastName, instant);
    }

    public boolean isConverted() {
        return convertedAt != null;
    }

    public Optional<Instant> conversionInstant() {
        return Optional.ofNullable(convertedAt);
    }
}
