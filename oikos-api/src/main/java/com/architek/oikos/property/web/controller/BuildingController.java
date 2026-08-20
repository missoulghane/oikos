package com.architek.oikos.property.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.AddBuildingCommand;
import com.architek.oikos.property.application.command.UpdateBuildingCommand;
import com.architek.oikos.property.application.port.in.AddBuildingUseCase;
import com.architek.oikos.property.application.port.in.UpdateBuildingUseCase;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.port.in.ListBuildingsByPropertyUseCase;
import com.architek.oikos.property.application.query.GetBuildingQuery;
import com.architek.oikos.property.application.query.ListBuildingsByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.web.request.AddBuildingRequest;
import com.architek.oikos.property.web.request.UpdateBuildingRequest;
import com.architek.oikos.property.web.response.BuildingResponse;
import com.architek.oikos.property.web.response.PagedBuildingResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@RestController
public class BuildingController {

    private final AddBuildingUseCase addBuildingUseCase;
    private final GetBuildingUseCase getBuildingUseCase;
    private final ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase;
    private final UpdateBuildingUseCase updateBuildingUseCase;

    public BuildingController(AddBuildingUseCase addBuildingUseCase,
                               GetBuildingUseCase getBuildingUseCase,
                               ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase,
                               UpdateBuildingUseCase updateBuildingUseCase) {
        this.addBuildingUseCase = addBuildingUseCase;
        this.getBuildingUseCase = getBuildingUseCase;
        this.listBuildingsByPropertyUseCase = listBuildingsByPropertyUseCase;
        this.updateBuildingUseCase = updateBuildingUseCase;
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/buildings")
    public PagedBuildingResponse list(@PathVariable String propertyId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        ListBuildingsByPropertyQuery query =
                new ListBuildingsByPropertyQuery(PropertyId.of(propertyId), PageRequest.of(page, size));
        return PagedBuildingResponse.from(listBuildingsByPropertyUseCase.listBuildings(query));
    }

    @PreAuthorize("@propertyAccess.managesBuilding(authentication, #id)")
    @GetMapping("/buildings/{id}")
    public BuildingResponse getById(@PathVariable String id) {
        return BuildingResponse.from(getBuildingUseCase.getBuilding(new GetBuildingQuery(BuildingId.of(id))));
    }

    /**
     * Renommer un batiment ou corriger son nombre d'etages. Meme garde que la
     * lecture ({@code managesBuilding}), qui remonte a la copropriete du batiment :
     * le chemin ne porte pas d'identifiant de copropriete a verifier.
     */
    @PreAuthorize("@propertyAccess.managesBuilding(authentication, #id)")
    @PutMapping("/buildings/{id}")
    public BuildingResponse update(@PathVariable String id, @Valid @RequestBody UpdateBuildingRequest request) {
        return BuildingResponse.from(updateBuildingUseCase.update(
                new UpdateBuildingCommand(BuildingId.of(id), request.name(), request.floorCount())));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/buildings")
    public ResponseEntity<Void> add(@PathVariable String propertyId, @Valid @RequestBody AddBuildingRequest request) {
        BuildingId id = addBuildingUseCase.add(new AddBuildingCommand(
                PropertyId.of(propertyId), request.name(), request.floorCount()));
        return ResponseEntity.created(URI.create("/api/v1/buildings/" + id)).build();
    }
}
