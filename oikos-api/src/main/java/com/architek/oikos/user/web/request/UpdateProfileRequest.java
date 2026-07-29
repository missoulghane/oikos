package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Email @Size(max = 150) String email) {
}
