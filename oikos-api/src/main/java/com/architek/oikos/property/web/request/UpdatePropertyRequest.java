package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePropertyRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 250) String address,
        // Facultative, comme dans l'agregat : une copropriete provisionnee par
        // l'inscription d'un cabinet n'en a pas forcement une.
        @Size(max = 100) String city) {
}
