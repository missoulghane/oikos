package com.architek.oikos.property.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.RemoveUnitTypePriceCommand;
import com.architek.oikos.property.application.command.SetUnitTypePriceCommand;
import com.architek.oikos.property.application.port.in.ListUnitTypePricesByPropertyUseCase;
import com.architek.oikos.property.application.port.in.RemoveUnitTypePriceUseCase;
import com.architek.oikos.property.application.port.in.SetUnitTypePriceUseCase;
import com.architek.oikos.property.application.query.ListUnitTypePricesByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.web.request.SetUnitTypePriceRequest;
import com.architek.oikos.property.web.response.UnitTypePriceResponse;

/**
 * Parametrage du prix par type de lot (UnitTypeDefinition) pour une property
 * (ex: appartement: 300, box: 100). Un type sans prix configure n'a
 * simplement pas d'entree - aucune valeur par defaut, aucune erreur.
 */
@RestController
public class UnitTypePricingController {

    private final SetUnitTypePriceUseCase setUnitTypePriceUseCase;
    private final ListUnitTypePricesByPropertyUseCase listUnitTypePricesByPropertyUseCase;
    private final RemoveUnitTypePriceUseCase removeUnitTypePriceUseCase;

    public UnitTypePricingController(SetUnitTypePriceUseCase setUnitTypePriceUseCase,
                                       ListUnitTypePricesByPropertyUseCase listUnitTypePricesByPropertyUseCase,
                                       RemoveUnitTypePriceUseCase removeUnitTypePriceUseCase) {
        this.setUnitTypePriceUseCase = setUnitTypePriceUseCase;
        this.listUnitTypePricesByPropertyUseCase = listUnitTypePricesByPropertyUseCase;
        this.removeUnitTypePriceUseCase = removeUnitTypePriceUseCase;
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/unit-type-prices")
    public List<UnitTypePriceResponse> list(@PathVariable String propertyId) {
        ListUnitTypePricesByPropertyQuery query = new ListUnitTypePricesByPropertyQuery(PropertyId.of(propertyId));
        return listUnitTypePricesByPropertyUseCase.listUnitTypePrices(query).stream()
                .map(UnitTypePriceResponse::from)
                .toList();
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PutMapping("/properties/{propertyId}/unit-type-prices/{unitTypeId}")
    public UnitTypePriceResponse set(@PathVariable String propertyId, @PathVariable String unitTypeId,
                                       @Valid @RequestBody SetUnitTypePriceRequest request) {
        SetUnitTypePriceCommand command = new SetUnitTypePriceCommand(
                PropertyId.of(propertyId), UnitTypeDefinitionId.of(unitTypeId), Price.of(request.price()));
        return UnitTypePriceResponse.from(setUnitTypePriceUseCase.set(command));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/properties/{propertyId}/unit-type-prices/{unitTypeId}")
    public void remove(@PathVariable String propertyId, @PathVariable String unitTypeId) {
        removeUnitTypePriceUseCase.remove(
                new RemoveUnitTypePriceCommand(PropertyId.of(propertyId), UnitTypeDefinitionId.of(unitTypeId)));
    }
}
