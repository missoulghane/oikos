package com.architek.oikos.property.application.usecase;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.BuildingConfiguration;
import com.architek.oikos.property.application.command.ConfigureExistingPropertyCommand;
import com.architek.oikos.property.application.command.UnitTypeConfiguration;
import com.architek.oikos.property.application.port.in.ConfigureExistingPropertyUseCase;
import com.architek.oikos.property.application.port.out.LedgerAccountProvisioningPort;
import com.architek.oikos.property.domain.exception.PropertyAlreadyConfiguredException;
import com.architek.oikos.property.domain.exception.PropertyConfigurationLimitExceededException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.ProjectedBudget;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;
import com.architek.oikos.shared.domain.pagination.PageRequest;

/**
 * Lays out an already-created property in a single transaction: dues mode,
 * unit types and their prices, buildings, units, bank accounts. This is what
 * the volunteer-syndic wizard commits when its recap is validated.
 *
 * <p>Sibling of {@link ConfigurePropertyService}, which does the same for a
 * property it creates itself. They cannot be merged: the wizard's property
 * already exists by then (registration creates account, property and party
 * together, because a Party is property-scoped), so there is nothing to create
 * here - and no default "OTHERS" unit type either, since the wizard always
 * names the types it wants.
 *
 * <p>Units are created with Shares.ZERO, like the sibling service: tantiemes
 * are entered later from each unit's own screen, which is also why SHARES mode
 * only records the projected budget here.
 */
@Component
public class ConfigureExistingPropertyService implements ConfigureExistingPropertyUseCase {

    private static final int PAGE_SIZE = 1;

    private final PropertyRepository propertyRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final UnitTypePricingRepository unitTypePricingRepository;
    private final LedgerAccountProvisioningPort ledgerAccountProvisioningPort;
    private final int maxUnitsPerRequest;

    public ConfigureExistingPropertyService(PropertyRepository propertyRepository,
                                             BuildingRepository buildingRepository,
                                             UnitRepository unitRepository,
                                             UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                             UnitTypePricingRepository unitTypePricingRepository,
                                             LedgerAccountProvisioningPort ledgerAccountProvisioningPort,
                                             @Value("${oikos.property.configure.max-units}") int maxUnitsPerRequest) {
        this.propertyRepository = propertyRepository;
        this.buildingRepository = buildingRepository;
        this.unitRepository = unitRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.unitTypePricingRepository = unitTypePricingRepository;
        this.ledgerAccountProvisioningPort = ledgerAccountProvisioningPort;
        this.maxUnitsPerRequest = maxUnitsPerRequest;
    }

    @Override
    @Transactional
    public void configure(ConfigureExistingPropertyCommand command) {
        Property property = propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(command.propertyId()));
        if (hasBuildings(command.propertyId())) {
            throw new PropertyAlreadyConfiguredException(command.propertyId());
        }
        int totalUnitCount = command.buildings().stream()
                .flatMap(building -> building.unitTypes().stream())
                .mapToInt(UnitTypeConfiguration::count)
                .sum();
        if (totalUnitCount > maxUnitsPerRequest) {
            throw new PropertyConfigurationLimitExceededException(totalUnitCount, maxUnitsPerRequest);
        }

        applyDuesConfiguration(property, command);
        Map<String, UnitTypeDefinitionId> unitTypeIdsByName = createUnitTypes(command);
        createStructure(command, unitTypeIdsByName);
        command.bankAccounts().forEach(bankAccount -> ledgerAccountProvisioningPort.provisionBankAccount(
                command.propertyId().value(), bankAccount.label(), bankAccount.bankAccountNumber()));
    }

    private void applyDuesConfiguration(Property property, ConfigureExistingPropertyCommand command) {
        Property configured = property.withDuesCalculationMode(command.duesCalculationMode());
        if (command.projectedBudget() != null) {
            configured = configured.withProjectedBudget(ProjectedBudget.of(command.projectedBudget()));
        }
        propertyRepository.save(configured);
    }

    /**
     * Find-or-create by name: the property may already carry types (nothing
     * forbids adding one before finishing the wizard), and re-using them keeps
     * a single definition per name, as the unique constraint expects.
     */
    private Map<String, UnitTypeDefinitionId> createUnitTypes(ConfigureExistingPropertyCommand command) {
        Map<String, UnitTypeDefinitionId> unitTypeIdsByName = new HashMap<>();
        for (ConfigureExistingPropertyCommand.UnitTypePricing unitType : command.unitTypes()) {
            UnitTypeDefinition definition = unitTypeDefinitionRepository
                    .findByPropertyIdAndName(command.propertyId(), unitType.name())
                    .orElseGet(() -> unitTypeDefinitionRepository.save(UnitTypeDefinition.create(
                            UnitTypeDefinitionId.newId(), command.propertyId(), unitType.name())));
            unitTypeIdsByName.put(unitType.name(), definition.getId());
            if (unitType.price() != null) {
                setPrice(command.propertyId(), definition.getId(), unitType.price());
            }
        }
        return unitTypeIdsByName;
    }

    private void setPrice(PropertyId propertyId, UnitTypeDefinitionId unitTypeId, BigDecimal price) {
        UnitTypePricing pricing = unitTypePricingRepository.findByUnitTypeId(unitTypeId)
                .map(existing -> existing.withPrice(Price.of(price)))
                .orElseGet(() -> UnitTypePricing.create(UnitTypePricingId.newId(), propertyId, unitTypeId,
                        Price.of(price)));
        unitTypePricingRepository.save(pricing);
    }

    private void createStructure(ConfigureExistingPropertyCommand command,
                                  Map<String, UnitTypeDefinitionId> unitTypeIdsByName) {
        for (BuildingConfiguration buildingConfiguration : command.buildings()) {
            Building building = buildingRepository.save(Building.create(BuildingId.newId(), command.propertyId(),
                    buildingConfiguration.name(), buildingConfiguration.floorCount()));
            for (UnitTypeConfiguration unitTypeConfiguration : buildingConfiguration.unitTypes()) {
                UnitTypeDefinitionId unitTypeId = unitTypeIdsByName.get(unitTypeConfiguration.unitTypeName());
                if (unitTypeId == null) {
                    // A building referencing a type the property does not declare would
                    // otherwise create units pointing at nothing.
                    throw new IllegalArgumentException(
                            "Unknown unit type in building configuration: " + unitTypeConfiguration.unitTypeName());
                }
                createUnits(building.getId(), command.propertyId(), unitTypeId,
                        unitTypeConfiguration.count());
            }
        }
    }

    /**
     * unitNumber is generated as "{type} {n}", restarting at 1 per (building,
     * type) - safe because this service refuses to run on a property that
     * already has buildings, so no number can collide with an existing one.
     */
    private void createUnits(BuildingId buildingId, PropertyId propertyId,
                              UnitTypeDefinitionId unitTypeId, int count) {
        for (int sequence = 1; sequence <= count; sequence++) {
            Unit savedUnit = unitRepository.save(Unit.create(UnitId.newId(), buildingId, propertyId,
                    Unit.generatedNumber(sequence), unitTypeId, Shares.of(BigDecimal.ZERO)));
            ledgerAccountProvisioningPort.provisionUnitReceivableAccount(propertyId.value(), savedUnit.getId().value());
        }
    }

    /** One row is enough to answer "is this property already laid out?". */
    private boolean hasBuildings(PropertyId propertyId) {
        return !buildingRepository.findAllByPropertyId(propertyId, PageRequest.of(0, PAGE_SIZE)).content().isEmpty();
    }
}
