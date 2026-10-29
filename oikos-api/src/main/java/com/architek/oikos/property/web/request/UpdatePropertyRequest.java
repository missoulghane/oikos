package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePropertyRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 250) String address,
        // Obligatoire a la modification, facultative dans l'agregat : une
        // copropriete provisionnee par l'inscription d'un cabinet peut naitre
        // sans ville, mais l'ecran de modification la fait renseigner.
        @NotBlank @Size(max = 100) String city) {
}
