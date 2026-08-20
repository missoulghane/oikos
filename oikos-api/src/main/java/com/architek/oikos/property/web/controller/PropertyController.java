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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.BuildingConfiguration;
import com.architek.oikos.property.application.command.ConfigureExistingPropertyCommand;
import com.architek.oikos.property.application.command.ConfigurePropertyCommand;
import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.command.SetProjectedBudgetCommand;
import com.architek.oikos.property.application.command.UnitTypeConfiguration;
import com.architek.oikos.property.application.command.UpdateDuesCalculationModeCommand;
import com.architek.oikos.property.application.command.UpdatePropertyCommand;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.ConfigureExistingPropertyUseCase;
import com.architek.oikos.property.application.port.in.ConfigurePropertyUseCase;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListPropertiesUseCase;
import com.architek.oikos.property.application.port.in.SetProjectedBudgetUseCase;
import com.architek.oikos.property.application.port.in.UpdateDuesCalculationModeUseCase;
import com.architek.oikos.property.application.port.in.UpdatePropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.application.query.ListPropertiesQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.web.request.AssignPropertyManagerRequest;
import com.architek.oikos.property.web.request.BuildingConfigurationRequest;
import com.architek.oikos.property.web.request.ConfigureExistingPropertyRequest;
import com.architek.oikos.property.web.request.ConfiguredBuildingRequest;
import com.architek.oikos.property.web.request.ConfiguredUnitCountRequest;
import com.architek.oikos.property.web.request.ConfigurePropertyRequest;
import com.architek.oikos.property.web.request.CreatePropertyRequest;
import com.architek.oikos.property.web.request.SetProjectedBudgetRequest;
import com.architek.oikos.property.web.request.UnitTypeConfigurationRequest;
import com.architek.oikos.property.web.request.UpdateDuesCalculationModeRequest;
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
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Gestion administrative des copropriétés. list retourne toutes les
 * copropriétés pour ADMIN, uniquement celles gérées par l'appelant sinon.
 * create/configure sont ouverts à ADMIN et à tout compte détenant déjà un
 * rôle ADMIN-tier (PROPERTY_BOARD_ADMIN/PROPERTY_MANAGER_ADMIN) sur au moins
 * une copropriété (voir PropertyAccessEvaluator.canCreateProperty) - le
 * créateur devient automatiquement gestionnaire de la copropriété créée,
 * avec le même rôle ADMIN-tier que celui qu'il détient déjà ailleurs (voir
 * GrantCreatorAsManagerUseCase) - sans plafond de cardinalité : un compte
 * PROPERTY_BOARD_ADMIN peut administrer plusieurs copropriétés, à
 * l'exception des flux d'auto-inscription publics
 * (register-property-board-admin/register-property-manager-admin), qui
 * restent des chemins de code distincts.
 */
@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final CreatePropertyUseCase createPropertyUseCase;
    private final ConfigurePropertyUseCase configurePropertyUseCase;
    private final ConfigureExistingPropertyUseCase configureExistingPropertyUseCase;
    private final GetPropertyUseCase getPropertyUseCase;
    private final ListPropertiesUseCase listPropertiesUseCase;
    private final UpdatePropertyUseCase updatePropertyUseCase;
    private final UpdateDuesCalculationModeUseCase updateDuesCalculationModeUseCase;
    private final SetProjectedBudgetUseCase setProjectedBudgetUseCase;
    private final GetUserAccessUseCase getUserAccessUseCase;
    private final GrantCreatorAsManagerUseCase grantCreatorAsManagerUseCase;
    private final AssignPropertyManagerUseCase assignPropertyManagerUseCase;

    public PropertyController(CreatePropertyUseCase createPropertyUseCase,
                                  ConfigurePropertyUseCase configurePropertyUseCase,
                                  ConfigureExistingPropertyUseCase configureExistingPropertyUseCase,
                                  GetPropertyUseCase getPropertyUseCase,
                                  ListPropertiesUseCase listPropertiesUseCase,
                                  UpdatePropertyUseCase updatePropertyUseCase,
                                  UpdateDuesCalculationModeUseCase updateDuesCalculationModeUseCase,
                                  SetProjectedBudgetUseCase setProjectedBudgetUseCase,
                                  GetUserAccessUseCase getUserAccessUseCase,
                                  GrantCreatorAsManagerUseCase grantCreatorAsManagerUseCase,
                                  AssignPropertyManagerUseCase assignPropertyManagerUseCase) {
        this.createPropertyUseCase = createPropertyUseCase;
        this.configurePropertyUseCase = configurePropertyUseCase;
        this.configureExistingPropertyUseCase = configureExistingPropertyUseCase;
        this.getPropertyUseCase = getPropertyUseCase;
        this.listPropertiesUseCase = listPropertiesUseCase;
        this.updatePropertyUseCase = updatePropertyUseCase;
        this.updateDuesCalculationModeUseCase = updateDuesCalculationModeUseCase;
        this.setProjectedBudgetUseCase = setProjectedBudgetUseCase;
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

    @PreAuthorize("@propertyAccess.canCreateProperty(authentication)")
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePropertyRequest request, Authentication authentication) {
        UserId userId = currentUserId(authentication);
        PropertyRole roleToGrant = resolveRoleToGrant(userId);
        PropertyId id = createPropertyUseCase.create(new CreatePropertyCommand(request.name(), request.address(), request.city()));
        grantCreatorAsManagerUseCase.grant(
                new GrantCreatorAsManagerCommand(userId, EntityId.of(id.asUuid()), roleToGrant));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + id)).build();
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #id)")
    @PutMapping("/{id}")
    public PropertyResponse update(@PathVariable String id, @Valid @RequestBody UpdatePropertyRequest request) {
        return PropertyResponse.from(updatePropertyUseCase.update(
                new UpdatePropertyCommand(PropertyId.of(id), request.name(), request.address(), request.city())));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #id)")
    @PutMapping("/{id}/dues-calculation-mode")
    public PropertyResponse updateDuesCalculationMode(@PathVariable String id,
                                                        @Valid @RequestBody UpdateDuesCalculationModeRequest request) {
        return PropertyResponse.from(updateDuesCalculationModeUseCase.updateMode(
                new UpdateDuesCalculationModeCommand(PropertyId.of(id), request.mode())));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #id)")
    @PutMapping("/{id}/projected-budget")
    public PropertyResponse setProjectedBudget(@PathVariable String id,
                                                @Valid @RequestBody SetProjectedBudgetRequest request) {
        return PropertyResponse.from(setProjectedBudgetUseCase.setProjectedBudget(
                new SetProjectedBudgetCommand(PropertyId.of(id), request.projectedBudget())));
    }

    @PreAuthorize("@propertyAccess.canCreateProperty(authentication)")
    @PostMapping("/configure")
    public ResponseEntity<Void> configure(@Valid @RequestBody ConfigurePropertyRequest request, Authentication authentication) {
        UserId userId = currentUserId(authentication);
        PropertyRole roleToGrant = resolveRoleToGrant(userId);
        PropertyId id = configurePropertyUseCase.configure(toCommand(request));
        grantCreatorAsManagerUseCase.grant(
                new GrantCreatorAsManagerCommand(userId, EntityId.of(id.asUuid()), roleToGrant));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + id)).build();
    }

    /**
     * Final step of the volunteer-syndic wizard: lays out a property that already
     * exists (registration created it together with the account and its party).
     * Accepts the wizard's short-lived onboarding token as well as an ordinary
     * board-admin session, so a wizard resumed after the token expired still goes
     * through - see PropertyAccessEvaluator.canConfigureOnboarding.
     */
    @PreAuthorize("@propertyAccess.canConfigureOnboarding(authentication, #id)")
    @PostMapping("/{id}/configuration")
    public ResponseEntity<Void> configureExisting(@PathVariable String id,
                                                   @Valid @RequestBody ConfigureExistingPropertyRequest request) {
        configureExistingPropertyUseCase.configure(toCommand(PropertyId.of(id), request));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.canInviteMemberOnProperty(authentication, #id)")
    @PostMapping("/{id}/managers")
    public ResponseEntity<Void> assignManager(@PathVariable String id, @Valid @RequestBody AssignPropertyManagerRequest request,
                                               Authentication authentication) {
        UserAccessView access = getUserAccessUseCase.getAccess(new GetUserAccessQuery(currentUserId(authentication)));
        // The invited member's role mirrors the ADMIN-tier role the *caller* already holds on
        // this specific property (a board admin invites a board member, a manager-firm admin
        // invites a manager-firm member) - never a value the request payload controls. A global
        // SYSTEM_ADMIN with no grant of their own on this property defaults to the manager-firm
        // (uncapped) tier.
        PropertyRole memberRole = access.rolesByProperty().getOrDefault(id, Set.of()).stream()
                .filter(PropertyRole::isAdminTier)
                .findFirst()
                .map(PropertyRole::memberTierEquivalent)
                .orElse(PropertyRole.PROPERTY_MANAGER_MEMBER);
        assignPropertyManagerUseCase.assign(
                new AssignPropertyManagerCommand(EntityId.of(id), EmailVO.of(request.email()), memberRole));
        return ResponseEntity.noContent().build();
    }

    private static UserId currentUserId(Authentication authentication) {
        return UserId.of(authentication.getName());
    }

    /**
     * The role to grant the creator of a new property: the same ADMIN-tier
     * role they already hold elsewhere (board vs manager-firm track), or
     * PROPERTY_MANAGER_ADMIN if they hold none yet - covers a platform
     * SYSTEM_ADMIN using this authenticated endpoint directly. Assumes a
     * caller holds at most one ADMIN-tier role type across all their
     * properties (see UserAccessView.dominantAdminTierRole).
     */
    private PropertyRole resolveRoleToGrant(UserId userId) {
        UserAccessView access = getUserAccessUseCase.getAccess(new GetUserAccessQuery(userId));
        return access.dominantAdminTierRole().orElse(PropertyRole.PROPERTY_MANAGER_ADMIN);
    }

    private static ConfigurePropertyCommand toCommand(ConfigurePropertyRequest request) {
        List<BuildingConfiguration> buildings = request.property().buildings().stream()
                .map(PropertyController::toBuildingConfiguration)
                .toList();
        return new ConfigurePropertyCommand(request.property().name(), request.property().address(),
                request.property().city(), buildings);
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

    private static ConfigureExistingPropertyCommand toCommand(PropertyId propertyId,
                                                                ConfigureExistingPropertyRequest request) {
        List<BuildingConfiguration> buildings = new ArrayList<>();
        for (int index = 0; index < request.buildings().size(); index++) {
            ConfiguredBuildingRequest building = request.buildings().get(index);
            // The wizard makes both optional; the domain requires both (Building).
            String name = building.name() == null || building.name().isBlank()
                    ? "Bâtiment " + (index + 1)
                    : building.name().trim();
            int floorCount = building.floorCount() == null ? 0 : building.floorCount();
            buildings.add(new BuildingConfiguration(name, floorCount, building.unitTypes().stream()
                    .filter(unitType -> unitType.count() > 0)
                    .map(unitType -> new UnitTypeConfiguration(unitType.unitTypeName(), unitType.count()))
                    .toList()));
        }
        return new ConfigureExistingPropertyCommand(propertyId, request.duesCalculationMode(),
                request.projectedBudget(),
                request.unitTypes().stream()
                        .map(unitType -> new ConfigureExistingPropertyCommand.UnitTypePricing(unitType.name(),
                                unitType.price()))
                        .toList(),
                buildings,
                request.bankAccounts() == null ? List.of() : request.bankAccounts().stream()
                        .map(bankAccount -> new ConfigureExistingPropertyCommand.BankAccountConfiguration(
                                bankAccount.label(), bankAccount.bankAccountNumber()))
                        .toList());
    }
}
