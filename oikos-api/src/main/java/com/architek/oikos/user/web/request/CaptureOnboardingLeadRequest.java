package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CaptureOnboardingLeadRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 200) String fullName) {
}
