package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record AddUnitOwnerRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotNull PartyType partyType,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @NotNull @DecimalMin(value = "0", inclusive = true) @DecimalMax(value = "100", inclusive = true) BigDecimal ownershipShare) {
}
