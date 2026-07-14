package com.architek.oikos.auth.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * identifier is deliberately not constrained to an email format: it can be the
 * account's own login or the linked contact's email/phone.
 */
public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {
}
