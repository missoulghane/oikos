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
import com.architek.oikos.property.application.command.AddUnitOwnerCommand;
import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.command.RemoveUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnerUseCase;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.port.in.RemoveUnitOwnershipUseCase;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.web.request.AddUnitOwnerRequest;
import com.architek.oikos.property.web.request.AddUnitOwnershipRequest;
import com.architek.oikos.property.web.response.UnitOwnershipResponse;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Rattachement des copropretaires (personnes physiques, via Party) a un lot.
 */
@RestController
public class UnitOwnershipController {

    private final AddUnitOwnershipUseCase addUnitOwnershipUseCase;
    private final AddUnitOwnerUseCase addUnitOwnerUseCase;
    private final ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;
    private final RemoveUnitOwnershipUseCase removeUnitOwnershipUseCase;

    public UnitOwnershipController(AddUnitOwnershipUseCase addUnitOwnershipUseCase,
                                   AddUnitOwnerUseCase addUnitOwnerUseCase,
                                   ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase,
                                   RemoveUnitOwnershipUseCase removeUnitOwnershipUseCase) {
        this.addUnitOwnershipUseCase = addUnitOwnershipUseCase;
        this.addUnitOwnerUseCase = addUnitOwnerUseCase;
        this.listUnitOwnershipsByUnitUseCase = listUnitOwnershipsByUnitUseCase;
        this.removeUnitOwnershipUseCase = removeUnitOwnershipUseCase;
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId) or @propertyAccess.ownsUnit(authentication, #unitId)")
    @GetMapping("/units/{unitId}/owners")
    public List<UnitOwnershipResponse> list(@PathVariable String unitId) {
        return listUnitOwnershipsByUnitUseCase.listUnitOwnerships(new ListUnitOwnershipsByUnitQuery(UnitId.of(unitId))).stream()
                .map(UnitOwnershipResponse::from)
                .toList();
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId)")
    @PostMapping("/units/{unitId}/owners")
    public ResponseEntity<Void> add(@PathVariable String unitId, @Valid @RequestBody AddUnitOwnershipRequest request) {
        UnitOwnershipId id = addUnitOwnershipUseCase.add(new AddUnitOwnershipCommand(
                UnitId.of(unitId), EntityId.of(request.partyId()), request.ownershipShare()));
        return ResponseEntity.created(URI.create("/api/v1/units/" + unitId + "/owners/" + id)).build();
    }

    /** Une adresse vide vaut pas d'adresse : le formulaire envoie l'un ou l'autre. */
    private static EmailVO emailOrNull(String email) {
        return email == null || email.isBlank() ? null : EmailVO.of(email);
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId)")
    @PostMapping("/units/{unitId}/owners/new-party")
    public ResponseEntity<Void> addWithNewParty(@PathVariable String unitId,
                                                    @Valid @RequestBody AddUnitOwnerRequest request) {
        UnitOwnershipId id = addUnitOwnerUseCase.add(new AddUnitOwnerCommand(UnitId.of(unitId), request.fullName(),
                request.partyType(), emailOrNull(request.email()), request.phone(), request.ownershipShare(),
                request.inviteOrDefault()));
        return ResponseEntity.created(URI.create("/api/v1/units/" + unitId + "/owners/" + id)).build();
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/units/{unitId}/owners/{id}")
    public void remove(@PathVariable String unitId, @PathVariable String id) {
        removeUnitOwnershipUseCase.remove(new RemoveUnitOwnershipCommand(UnitOwnershipId.of(id)));
    }
}
