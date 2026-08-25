package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * floor est facultatif ("etage inconnu" est un etat legitime, cf. Unit) ;
 * quand il est fourni, sa coherence avec le nombre d'etages de l'immeuble est
 * verifiee par AddUnitService, qui est le seul a connaitre le Building.
 */
public record AddUnitRequest(
        @NotBlank @Size(max = 20) String unitNumber,
        @NotBlank String unitTypeId,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal shares,
        @Min(0) Integer floor) {
}
