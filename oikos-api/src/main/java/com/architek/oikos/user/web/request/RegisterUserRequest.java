package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.domain.model.Role;

public record RegisterUserRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        // Facultatif côté API, exigé par les formulaires d'inscription : les
        // comptes déjà créés n'en ont pas, et un @NotBlank ici rejetterait
        // aussi les clients qui n'ont pas encore été mis à jour.
        @Size(max = 20) String phone,
        @NotBlank @Size(min = RawPassword.MIN_LENGTH) String password,
        Role role,
        String returnTo,
        String invitationToken,
        String unitId) {
}
