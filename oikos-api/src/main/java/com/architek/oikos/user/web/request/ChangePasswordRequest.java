package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = RawPassword.MIN_LENGTH) String newPassword) {
}
