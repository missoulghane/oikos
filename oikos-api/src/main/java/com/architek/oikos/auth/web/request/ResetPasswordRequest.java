package com.architek.oikos.auth.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = RawPassword.MIN_LENGTH) String newPassword) {
}
