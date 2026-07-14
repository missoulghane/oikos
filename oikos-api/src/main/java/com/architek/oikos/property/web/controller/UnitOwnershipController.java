package com.architek.oikos.property.web.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.command.RemoveUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.port.in.RemoveUnitOwnershipUseCase;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.web.request.AddUnitOwnershipRequest;
import com.architek.oikos.property.web.response.UnitOwnershipResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Rattachement des copropretaires (personnes physiques, via Contact) a un lot.
 */
@RestController
// Pour le moment aucune de gestion de droits (à mettre en place plus tard)
public class UnitOwnershipController {

    private final AddUnitOwnershipUseCase addUnitOwnershipUseCase;
    private final ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;
    private final RemoveUnitOwnershipUseCase removeUnitOwnershipUseCase;

    public UnitOwnershipController(AddUnitOwnershipUseCase addUnitOwnershipUseCase,
                                   ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase,
                                   RemoveUnitOwnershipUseCase removeUnitOwnershipUseCase) {
        this.addUnitOwnershipUseCase = addUnitOwnershipUseCase;
        this.listUnitOwnershipsByUnitUseCase = listUnitOwnershipsByUnitUseCase;
        this.removeUnitOwnershipUseCase = removeUnitOwnershipUseCase;
    }

    @GetMapping("/units/{unitId}/owners")
    public List<UnitOwnershipResponse> list(@PathVariable String unitId) {
        return listUnitOwnershipsByUnitUseCase.listUnitOwnerships(new ListUnitOwnershipsByUnitQuery(UnitId.of(unitId))).stream()
                .map(UnitOwnershipResponse::from)
                .toList();
    }

    @PostMapping("/units/{unitId}/owners")
    public ResponseEntity<Void> add(@PathVariable String unitId, @Valid @RequestBody AddUnitOwnershipRequest request) {
        UnitOwnershipId id = addUnitOwnershipUseCase.add(new AddUnitOwnershipCommand(
                UnitId.of(unitId), EntityId.of(request.contactId()), request.ownershipShare()));
        return ResponseEntity.created(URI.create("/api/v1/units/" + unitId + "/owners/" + id)).build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/units/{unitId}/owners/{id}")
    public void remove(@PathVariable String unitId, @PathVariable String id) {
        removeUnitOwnershipUseCase.remove(new RemoveUnitOwnershipCommand(UnitOwnershipId.of(id)));
    }
}
