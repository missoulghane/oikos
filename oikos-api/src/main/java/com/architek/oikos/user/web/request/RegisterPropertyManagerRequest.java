package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record RegisterPropertyManagerRequest(
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @Size(max = 150) String login,
        @NotBlank @Size(min = RawPassword.MIN_LENGTH) String password,
        @NotBlank @Size(max = 100) String propertyName,
        @NotBlank @Size(max = 250) String propertyAddress) {
}
