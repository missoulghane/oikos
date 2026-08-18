package com.architek.oikos.property.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.AddUnitCommand;
import com.architek.oikos.property.application.command.UpdateUnitSharesCommand;
import com.architek.oikos.property.application.port.in.AddUnitUseCase;
import com.architek.oikos.property.application.port.in.CountUnitsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.port.in.UpdateUnitSharesUseCase;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.application.query.CountUnitsByPropertyQuery;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.UnitSortField;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.web.request.AddUnitRequest;
import com.architek.oikos.property.web.request.UpdateUnitSharesRequest;
import com.architek.oikos.property.web.response.UnitResponse;
import com.architek.oikos.property.web.response.PagedUnitResponse;
import com.architek.oikos.property.web.response.UnitCountResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
public class UnitController {

    private final AddUnitUseCase addUnitUseCase;
    private final GetUnitUseCase getUnitUseCase;
    private final ListUnitsByBuildingUseCase listUnitsByBuildingUseCase;
    private final UpdateUnitSharesUseCase updateUnitSharesUseCase;
    private final CountUnitsByPropertyUseCase countUnitsByPropertyUseCase;

    public UnitController(AddUnitUseCase addUnitUseCase,
                          GetUnitUseCase getUnitUseCase,
                          ListUnitsByBuildingUseCase listUnitsByBuildingUseCase,
                          UpdateUnitSharesUseCase updateUnitSharesUseCase,
                          CountUnitsByPropertyUseCase countUnitsByPropertyUseCase) {
        this.addUnitUseCase = addUnitUseCase;
        this.getUnitUseCase = getUnitUseCase;
        this.listUnitsByBuildingUseCase = listUnitsByBuildingUseCase;
        this.updateUnitSharesUseCase = updateUnitSharesUseCase;
        this.countUnitsByPropertyUseCase = countUnitsByPropertyUseCase;
    }

    /**
     * The lot count of a whole copropriété, which no other endpoint gives:
     * units are listed per building, so counting them meant one call per
     * building and summing the totals on the client.
     */
    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/units/count")
    public UnitCountResponse count(@PathVariable String propertyId) {
        return new UnitCountResponse(
                countUnitsByPropertyUseCase.countUnits(new CountUnitsByPropertyQuery(EntityId.of(propertyId))));
    }

    @PreAuthorize("@propertyAccess.managesBuilding(authentication, #buildingId)")
    @GetMapping("/buildings/{buildingId}/units")
    public PagedUnitResponse list(@PathVariable String buildingId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) OwnershipStatus ownershipStatus,
                                  @RequestParam(required = false) UnitSortField sortBy,
                                  @RequestParam(required = false) SortDirection sortDirection) {
        ListUnitsByBuildingQuery query = new ListUnitsByBuildingQuery(
                BuildingId.of(buildingId), PageRequest.of(page, size), search, ownershipStatus, sortBy, sortDirection);
        return PagedUnitResponse.from(listUnitsByBuildingUseCase.listUnits(query));
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #id) or @propertyAccess.ownsUnit(authentication, #id)")
    @GetMapping("/units/{id}")
    public UnitResponse getById(@PathVariable String id) {
        return UnitResponse.from(getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(id))));
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #id)")
    @PutMapping("/units/{id}/shares")
    public UnitResponse updateShares(@PathVariable String id, @Valid @RequestBody UpdateUnitSharesRequest request) {
        return UnitResponse.from(updateUnitSharesUseCase.updateShares(
                new UpdateUnitSharesCommand(UnitId.of(id), request.shares())));
    }

    @PreAuthorize("@propertyAccess.managesBuilding(authentication, #buildingId)")
    @PostMapping("/buildings/{buildingId}/units")
    public ResponseEntity<Void> add(@PathVariable String buildingId, @Valid @RequestBody AddUnitRequest request) {
        UnitId id = addUnitUseCase.add(new AddUnitCommand(
                BuildingId.of(buildingId), request.unitNumber(), UnitTypeDefinitionId.of(request.unitTypeId()),
                request.shares()));
        return ResponseEntity.created(URI.create("/api/v1/units/" + id)).build();
    }
}
