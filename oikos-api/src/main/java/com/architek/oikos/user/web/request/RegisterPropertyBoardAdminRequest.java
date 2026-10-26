package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Step 1 of the volunteer-syndic wizard. acceptedTerms is a primitive so a
 * missing field reads as false and fails @AssertTrue, rather than slipping
 * through as null (which @AssertTrue treats as valid).
 */
public record RegisterPropertyBoardAdminRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @NotBlank @Size(min = RawPassword.MIN_LENGTH) String password,
        @AssertTrue(message = "the terms of sale and use must be accepted") boolean acceptedTerms) {
}
