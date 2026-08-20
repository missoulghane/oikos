package com.architek.oikos.party.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record UpdatePartyRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotNull PartyType partyType,
        /** Facultatif, comme à la création : un contact peut n'avoir qu'un téléphone. */
        @Email @Size(max = 150) String email,
        @Size(max = 20) String phone) {
}
