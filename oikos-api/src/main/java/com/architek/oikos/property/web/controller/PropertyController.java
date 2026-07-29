package com.architek.oikos.property.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.BuildingConfiguration;
import com.architek.oikos.property.application.command.ConfigurePropertyCommand;
import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.command.UnitTypeConfiguration;
import com.architek.oikos.property.application.command.UpdatePropertyCommand;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.ConfigurePropertyUseCase;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListPropertiesUseCase;
import com.architek.oikos.property.application.port.in.UpdatePropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.application.query.ListPropertiesQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.web.request.AssignPropertyManagerRequest;
import com.architek.oikos.property.web.request.BuildingConfigurationRequest;
import com.architek.oikos.property.web.request.ConfigurePropertyRequest;
import com.architek.oikos.property.web.request.CreatePropertyRequest;
import com.architek.oikos.property.web.request.UnitTypeConfigurationRequest;
import com.architek.oikos.property.web.request.UpdatePropertyRequest;
import com.architek.oikos.property.web.response.PropertyResponse;
import com.architek.oikos.property.web.response.PagedPropertyResponse;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.AssignPropertyManagerCommand;
import com.architek.oikos.user.application.command.GrantCreatorAsManagerCommand;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.AssignPropertyManagerUseCase;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.application.port.in.GrantCreatorAsManagerUseCase;
import com.architek.oikos.user.application.query.GetUserAccessQuery;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Gestion administrative des copropriétés. list retourne toutes les
 * copropriétés pour ADMIN, uniquement celles gérées par l'appelant sinon.
 * create/configure sont ouverts à ADMIN et à tout compte gérant déjà au
 * moins une copropriété (voir PropertyAccessEvaluator.isManagerOfAny) - le
 * créateur devient automatiquement gestionnaire de la copropriété créée
 * (voir GrantCreatorAsManagerUseCase), à l'exception du flux d'auto-inscription
 * public (register-property-manager), qui reste un chemin de code distinct.
 */
@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final CreatePropertyUseCase createPropertyUseCase;
    private final ConfigurePropertyUseCase configurePropertyUseCase;
    private final GetPropertyUseCase getPropertyUseCase;
    private final ListPropertiesUseCase listPropertiesUseCase;
    private final UpdatePropertyUseCase updatePropertyUseCase;
    private final GetUserAccessUseCase getUserAccessUseCase;
    private final GrantCreatorAsManagerUseCase grantCreatorAsManagerUseCase;
    private final AssignPropertyManagerUseCase assignPropertyManagerUseCase;

    public PropertyController(CreatePropertyUseCase createPropertyUseCase,
                                  ConfigurePropertyUseCase configurePropertyUseCase,
                                  GetPropertyUseCase getPropertyUseCase,
                                  ListPropertiesUseCase listPropertiesUseCase,
                                  UpdatePropertyUseCase updatePropertyUseCase,
                                  GetUserAccessUseCase getUserAccessUseCase,
                                  GrantCreatorAsManagerUseCase grantCreatorAsManagerUseCase,
                                  AssignPropertyManagerUseCase assignPropertyManagerUseCase) {
        this.createPropertyUseCase = createPropertyUseCase;
        this.configurePropertyUseCase = configurePropertyUseCase;
        this.getPropertyUseCase = getPropertyUseCase;
        this.listPropertiesUseCase = listPropertiesUseCase;
        this.updatePropertyUseCase = updatePropertyUseCase;
        this.getUserAccessUseCase = getUserAccessUseCase;
        this.grantCreatorAsManagerUseCase = grantCreatorAsManagerUseCase;
        this.assignPropertyManagerUseCase = assignPropertyManagerUseCase;
    }

    @GetMapping
    public PagedPropertyResponse list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          Authentication authentication) {
        UserAccessView access = getUserAccessUseCase.getAccess(new GetUserAccessQuery(currentUserId(authentication)));
        if (access.isAdmin()) {
            return PagedPropertyResponse.from(
                    listPropertiesUseCase.listProperties(new ListPropertiesQuery(PageRequest.of(page, size))));
        }
        List<PropertyView> managed = access.managedPropertyIds().stream()
                .map(id -> getPropertyUseCase.getProperty(new GetPropertyQuery(PropertyId.of(id))))
                .toList();
        return PagedPropertyResponse.from(Page.of(managed, 0, Math.max(managed.size(), 1), managed.size()));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #id)")
    @GetMapping("/{id}")
    public PropertyResponse getById(@PathVariable String id) {
        return PropertyResponse.from(getPropertyUseCase.getProperty(new GetPropertyQuery(PropertyId.of(id))));
    }

    @PreAuthorize("@propertyAccess.isManagerOfAny(authentication)")
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePropertyRequest request, Authentication authentication) {
        PropertyId id = createPropertyUseCase.create(new CreatePropertyCommand(request.name(), request.address()));
        grantCreatorAsManagerUseCase.grant(
                new GrantCreatorAsManagerCommand(currentUserId(authentication), EntityId.of(id.asUuid())));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + id)).build();
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #id)")
    @PutMapping("/{id}")
    public PropertyResponse update(@PathVariable String id, @Valid @RequestBody UpdatePropertyRequest request) {
        return PropertyResponse.from(updatePropertyUseCase.update(
                new UpdatePropertyCommand(PropertyId.of(id), request.name(), request.address())));
    }

    @PreAuthorize("@propertyAccess.isManagerOfAny(authentication)")
    @PostMapping("/configure")
    public ResponseEntity<Void> configure(@Valid @RequestBody ConfigurePropertyRequest request, Authentication authentication) {
        PropertyId id = configurePropertyUseCase.configure(toCommand(request));
        grantCreatorAsManagerUseCase.grant(
                new GrantCreatorAsManagerCommand(currentUserId(authentication), EntityId.of(id.asUuid())));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + id)).build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/managers")
    public ResponseEntity<Void> assignManager(@PathVariable String id, @Valid @RequestBody AssignPropertyManagerRequest request) {
        assignPropertyManagerUseCase.assign(
                new AssignPropertyManagerCommand(EntityId.of(id), EmailVO.of(request.email())));
        return ResponseEntity.noContent().build();
    }

    private static UserId currentUserId(Authentication authentication) {
        return UserId.of(authentication.getName());
    }

    private static ConfigurePropertyCommand toCommand(ConfigurePropertyRequest request) {
        List<BuildingConfiguration> buildings = request.property().buildings().stream()
                .map(PropertyController::toBuildingConfiguration)
                .toList();
        return new ConfigurePropertyCommand(request.property().name(), request.property().address(), buildings);
    }

    private static BuildingConfiguration toBuildingConfiguration(BuildingConfigurationRequest request) {
        List<UnitTypeConfiguration> unitTypes = request.unitTypes().stream()
                .map(PropertyController::toUnitTypeConfiguration)
                .toList();
        return new BuildingConfiguration(request.name(), request.floorCount(), unitTypes);
    }

    private static UnitTypeConfiguration toUnitTypeConfiguration(UnitTypeConfigurationRequest request) {
        return new UnitTypeConfiguration(request.unitTypeName(), request.count());
    }
}
