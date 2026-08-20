package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record RegisterPropertyBoardAdminRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @NotBlank @Size(min = RawPassword.MIN_LENGTH) String password,
        @NotBlank @Size(max = 100) String propertyName,
        @NotBlank @Size(max = 250) String propertyAddress,
        // Facultative comme partout ailleurs : le wizard la demande, le
        // formulaire court d'un cabinet ne s'en occupe pas encore.
        @Size(max = 100) String propertyCity) {
}
