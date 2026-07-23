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
import com.architek.oikos.property.application.port.in.AddUnitUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.web.request.AddUnitRequest;
import com.architek.oikos.property.web.response.UnitResponse;
import com.architek.oikos.property.web.response.PagedUnitResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@RestController
// Pour le moment aucune de gestion de droits (à mettre en place plus tard)
public class UnitController {

    private final AddUnitUseCase addUnitUseCase;
    private final GetUnitUseCase getUnitUseCase;
    private final ListUnitsByBuildingUseCase listUnitsByBuildingUseCase;

    public UnitController(AddUnitUseCase addUnitUseCase,
                          GetUnitUseCase getUnitUseCase,
                          ListUnitsByBuildingUseCase listUnitsByBuildingUseCase) {
        this.addUnitUseCase = addUnitUseCase;
        this.getUnitUseCase = getUnitUseCase;
        this.listUnitsByBuildingUseCase = listUnitsByBuildingUseCase;
    }

    @GetMapping("/buildings/{buildingId}/units")
    public PagedUnitResponse list(@PathVariable String buildingId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        ListUnitsByBuildingQuery query = new ListUnitsByBuildingQuery(BuildingId.of(buildingId), PageRequest.of(page, size));
        return PagedUnitResponse.from(listUnitsByBuildingUseCase.listUnits(query));
    }

    @GetMapping("/units/{id}")
    public UnitResponse getById(@PathVariable String id) {
        return UnitResponse.from(getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(id))));
    }

    @PostMapping("/buildings/{buildingId}/units")
    public ResponseEntity<Void> add(@PathVariable String buildingId, @Valid @RequestBody AddUnitRequest request) {
        UnitId id = addUnitUseCase.add(new AddUnitCommand(
                BuildingId.of(buildingId), request.unitNumber(), UnitTypeDefinitionId.of(request.unitTypeId()),
                request.shares()));
        return ResponseEntity.created(URI.create("/api/v1/units/" + id)).build();
    }
}
