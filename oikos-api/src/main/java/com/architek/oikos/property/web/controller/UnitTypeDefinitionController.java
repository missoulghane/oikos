package com.architek.oikos.property.web.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.AddUnitTypeDefinitionCommand;
import com.architek.oikos.property.application.command.RemoveUnitTypeDefinitionCommand;
import com.architek.oikos.property.application.port.in.AddUnitTypeDefinitionUseCase;
import com.architek.oikos.property.application.port.in.ListUnitTypeDefinitionsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.RemoveUnitTypeDefinitionUseCase;
import com.architek.oikos.property.application.query.ListUnitTypeDefinitionsByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.web.request.AddUnitTypeDefinitionRequest;
import com.architek.oikos.property.web.response.UnitTypeDefinitionResponse;

/**
 * Types de lot propres a une property (remplace l'ancien enum global
 * UnitType) - une ligne "OTHERS" existe toujours par defaut (creee a la
 * creation de la property).
 */
// Pour le moment aucune de gestion de droits (à mettre en place plus tard)
@RestController
public class UnitTypeDefinitionController {

    private final AddUnitTypeDefinitionUseCase addUnitTypeDefinitionUseCase;
    private final ListUnitTypeDefinitionsByPropertyUseCase listUnitTypeDefinitionsByPropertyUseCase;
    private final RemoveUnitTypeDefinitionUseCase removeUnitTypeDefinitionUseCase;

    public UnitTypeDefinitionController(AddUnitTypeDefinitionUseCase addUnitTypeDefinitionUseCase,
                                          ListUnitTypeDefinitionsByPropertyUseCase listUnitTypeDefinitionsByPropertyUseCase,
                                          RemoveUnitTypeDefinitionUseCase removeUnitTypeDefinitionUseCase) {
        this.addUnitTypeDefinitionUseCase = addUnitTypeDefinitionUseCase;
        this.listUnitTypeDefinitionsByPropertyUseCase = listUnitTypeDefinitionsByPropertyUseCase;
        this.removeUnitTypeDefinitionUseCase = removeUnitTypeDefinitionUseCase;
    }

    @GetMapping("/properties/{propertyId}/unit-types")
    public List<UnitTypeDefinitionResponse> list(@PathVariable String propertyId) {
        ListUnitTypeDefinitionsByPropertyQuery query = new ListUnitTypeDefinitionsByPropertyQuery(PropertyId.of(propertyId));
        return listUnitTypeDefinitionsByPropertyUseCase.listUnitTypeDefinitions(query).stream()
                .map(UnitTypeDefinitionResponse::from)
                .toList();
    }

    @PostMapping("/properties/{propertyId}/unit-types")
    public ResponseEntity<Void> add(@PathVariable String propertyId,
                                      @Valid @RequestBody AddUnitTypeDefinitionRequest request) {
        UnitTypeDefinitionId id = addUnitTypeDefinitionUseCase.add(
                new AddUnitTypeDefinitionCommand(PropertyId.of(propertyId), request.name()));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + propertyId + "/unit-types/" + id)).build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/properties/{propertyId}/unit-types/{id}")
    public void remove(@PathVariable String propertyId, @PathVariable String id) {
        removeUnitTypeDefinitionUseCase.remove(new RemoveUnitTypeDefinitionCommand(UnitTypeDefinitionId.of(id)));
    }
}
