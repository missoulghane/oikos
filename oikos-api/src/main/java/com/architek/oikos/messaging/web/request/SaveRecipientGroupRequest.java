package com.architek.oikos.messaging.web.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Création et modification partagent la même forme : l'écran enregistre le groupe entier. */
public record SaveRecipientGroupRequest(@NotBlank @Size(max = 120) String name,
                                          @NotEmpty List<String> memberUserIds) {
}
