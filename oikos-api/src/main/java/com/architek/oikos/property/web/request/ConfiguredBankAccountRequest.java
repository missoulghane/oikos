package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Bank account declared during onboarding - see AddBankAccountRequest for the field semantics. */
public record ConfiguredBankAccountRequest(
        @NotBlank @Size(max = 200) String label,
        @Size(max = 64) String bankAccountNumber) {
}
