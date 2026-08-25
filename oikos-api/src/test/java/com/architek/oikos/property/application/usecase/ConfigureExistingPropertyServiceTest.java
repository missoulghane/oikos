package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.BuildingConfiguration;
import com.architek.oikos.property.application.command.ConfigureExistingPropertyCommand;
import com.architek.oikos.property.application.command.UnitTypeConfiguration;
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
import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@ExtendWith(MockitoExtension.class)
class ConfigureExistingPropertyServiceTest {

    private static final int MAX_UNITS = 500;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Mock
    private UnitTypePricingRepository unitTypePricingRepository;

    @Mock
    private LedgerAccountProvisioningPort ledgerAccountProvisioningPort;

    private ConfigureExistingPropertyService newService(int maxUnitsPerRequest) {
        return new ConfigureExistingPropertyService(propertyRepository, buildingRepository, unitRepository,
                unitTypeDefinitionRepository, unitTypePricingRepository, ledgerAccountProvisioningPort,
                maxUnitsPerRequest);
    }

    private void givenExistingUnconfiguredProperty(PropertyId propertyId) {
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Résidence Exemple", "12 rue Exemple")));
        when(buildingRepository.findAllByPropertyId(eq(propertyId), any(PageRequest.class)))
                .thenReturn(Page.of(List.of(), 0, 1, 0));
    }

    private static ConfigureExistingPropertyCommand command(PropertyId propertyId, DuesCalculationMode mode,
                                                             BigDecimal projectedBudget,
                                                             List<ConfigureExistingPropertyCommand.UnitTypePricing> unitTypes,
                                                             List<BuildingConfiguration> buildings,
                                                             List<ConfigureExistingPropertyCommand.BankAccountConfiguration> bankAccounts) {
        return new ConfigureExistingPropertyCommand(propertyId, mode, projectedBudget, unitTypes, buildings,
                bankAccounts);
    }

    @Test
    void configuring_creates_the_declared_types_their_prices_and_the_numbered_units_of_each_building() {
        PropertyId propertyId = PropertyId.newId();
        givenExistingUnconfiguredProperty(propertyId);
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitTypeDefinitionRepository.findByPropertyIdAndName(eq(propertyId), any()))
                .thenReturn(Optional.empty());
        when(unitTypeDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitTypePricingRepository.findByUnitTypeId(any())).thenReturn(Optional.empty());
        when(unitTypePricingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService(MAX_UNITS).configure(command(propertyId, DuesCalculationMode.FLAT_RATE, null,
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", new BigDecimal("50")),
                        new ConfigureExistingPropertyCommand.UnitTypePricing("Box", new BigDecimal("15"))),
                List.of(new BuildingConfiguration("Bâtiment 1", 0,
                        List.of(new UnitTypeConfiguration("Appartement", 2), new UnitTypeConfiguration("Box", 1)))),
                List.of()));

        ArgumentCaptor<Unit> unitCaptor = ArgumentCaptor.forClass(Unit.class);
        verify(unitRepository, org.mockito.Mockito.times(3)).save(unitCaptor.capture());
        assertThat(unitCaptor.getAllValues()).extracting(Unit::getUnitNumber)
                .containsExactly("N° 1", "N° 2", "N° 1");
        assertThat(unitCaptor.getAllValues()).allSatisfy(
                unit -> assertThat(unit.getShares().value()).isEqualByComparingTo(BigDecimal.ZERO));

        ArgumentCaptor<UnitTypeDefinition> typeCaptor = ArgumentCaptor.forClass(UnitTypeDefinition.class);
        verify(unitTypeDefinitionRepository, org.mockito.Mockito.times(2)).save(typeCaptor.capture());
        assertThat(typeCaptor.getAllValues()).extracting(UnitTypeDefinition::getName)
                .containsExactly("Appartement", "Box");

        ArgumentCaptor<UnitTypePricing> priceCaptor = ArgumentCaptor.forClass(UnitTypePricing.class);
        verify(unitTypePricingRepository, org.mockito.Mockito.times(2)).save(priceCaptor.capture());
        assertThat(priceCaptor.getAllValues()).extracting(pricing -> pricing.getPrice().value())
                .containsExactly(new BigDecimal("50"), new BigDecimal("15"));
    }

    @Test
    void the_default_others_unit_type_is_not_created_unlike_the_property_creating_sibling() {
        PropertyId propertyId = PropertyId.newId();
        givenExistingUnconfiguredProperty(propertyId);
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitTypeDefinitionRepository.findByPropertyIdAndName(eq(propertyId), any())).thenReturn(Optional.empty());
        when(unitTypeDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService(MAX_UNITS).configure(command(propertyId, DuesCalculationMode.FLAT_RATE, null,
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", null)),
                List.of(new BuildingConfiguration("Bâtiment 1", 0, List.of(new UnitTypeConfiguration("Appartement", 1)))),
                List.of()));

        ArgumentCaptor<UnitTypeDefinition> typeCaptor = ArgumentCaptor.forClass(UnitTypeDefinition.class);
        verify(unitTypeDefinitionRepository).save(typeCaptor.capture());
        assertThat(typeCaptor.getValue().getName()).isEqualTo("Appartement");
        assertThat(typeCaptor.getAllValues()).extracting(UnitTypeDefinition::getName)
                .doesNotContain(UnitTypeDefinition.DEFAULT_NAME);
        verify(unitTypePricingRepository, never()).save(any());
    }

    @Test
    void an_existing_type_of_the_same_name_is_reused_rather_than_duplicated() {
        PropertyId propertyId = PropertyId.newId();
        givenExistingUnconfiguredProperty(propertyId);
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UnitTypeDefinition existing = UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), propertyId, "Appartement");
        when(unitTypeDefinitionRepository.findByPropertyIdAndName(propertyId, "Appartement"))
                .thenReturn(Optional.of(existing));

        newService(MAX_UNITS).configure(command(propertyId, DuesCalculationMode.FLAT_RATE, null,
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", null)),
                List.of(new BuildingConfiguration("Bâtiment 1", 0, List.of(new UnitTypeConfiguration("Appartement", 1)))),
                List.of()));

        verify(unitTypeDefinitionRepository, never()).save(any());
        ArgumentCaptor<Unit> unitCaptor = ArgumentCaptor.forClass(Unit.class);
        verify(unitRepository).save(unitCaptor.capture());
        assertThat(unitCaptor.getValue().getUnitTypeId()).isEqualTo(existing.getId());
    }

    @Test
    void shares_mode_records_the_projected_budget_on_the_property() {
        PropertyId propertyId = PropertyId.newId();
        givenExistingUnconfiguredProperty(propertyId);
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitTypeDefinitionRepository.findByPropertyIdAndName(eq(propertyId), any())).thenReturn(Optional.empty());
        when(unitTypeDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService(MAX_UNITS).configure(command(propertyId, DuesCalculationMode.SHARES, new BigDecimal("120000"),
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", null)),
                List.of(new BuildingConfiguration("Bâtiment 1", 0, List.of(new UnitTypeConfiguration("Appartement", 1)))),
                List.of()));

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());
        assertThat(propertyCaptor.getValue().getDuesCalculationMode()).isEqualTo(DuesCalculationMode.SHARES);
        assertThat(propertyCaptor.getValue().getProjectedBudget()).isPresent();
        assertThat(propertyCaptor.getValue().getProjectedBudget().get().value()).isEqualByComparingTo("120000");
    }

    @Test
    void declared_bank_accounts_are_provisioned_with_their_free_text_number() {
        PropertyId propertyId = PropertyId.newId();
        givenExistingUnconfiguredProperty(propertyId);
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitTypeDefinitionRepository.findByPropertyIdAndName(eq(propertyId), any())).thenReturn(Optional.empty());
        when(unitTypeDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService(MAX_UNITS).configure(command(propertyId, DuesCalculationMode.FLAT_RATE, null,
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", null)),
                List.of(new BuildingConfiguration("Bâtiment 1", 0, List.of(new UnitTypeConfiguration("Appartement", 1)))),
                List.of(new ConfigureExistingPropertyCommand.BankAccountConfiguration("Attijariwafa Bank", "0077800001234"),
                        new ConfigureExistingPropertyCommand.BankAccountConfiguration("CIH", null))));

        verify(ledgerAccountProvisioningPort).provisionBankAccount(propertyId.value(), "Attijariwafa Bank", "0077800001234");
        verify(ledgerAccountProvisioningPort).provisionBankAccount(propertyId.value(), "CIH", null);
    }

    @Test
    void a_property_that_already_has_buildings_is_rejected_so_a_replay_never_duplicates_its_units() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Résidence Exemple", "12 rue Exemple")));
        when(buildingRepository.findAllByPropertyId(eq(propertyId), any(PageRequest.class))).thenReturn(
                Page.of(List.of(Building.create(BuildingId.newId(), propertyId, "Bâtiment 1", 0)), 0, 1, 1));

        assertThatThrownBy(() -> newService(MAX_UNITS).configure(command(propertyId, DuesCalculationMode.FLAT_RATE, null,
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", null)),
                List.of(new BuildingConfiguration("Bâtiment 1", 0, List.of(new UnitTypeConfiguration("Appartement", 1)))),
                List.of())))
                .isInstanceOf(PropertyAlreadyConfiguredException.class);

        verify(unitRepository, never()).save(any());
        verify(buildingRepository, never()).save(any());
    }

    @Test
    void a_missing_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService(MAX_UNITS).configure(command(propertyId, DuesCalculationMode.FLAT_RATE, null,
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", null)),
                List.of(new BuildingConfiguration("Bâtiment 1", 0, List.of(new UnitTypeConfiguration("Appartement", 1)))),
                List.of())))
                .isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void the_total_unit_count_is_capped_before_anything_is_written() {
        PropertyId propertyId = PropertyId.newId();
        givenExistingUnconfiguredProperty(propertyId);

        assertThatThrownBy(() -> newService(2).configure(command(propertyId, DuesCalculationMode.FLAT_RATE, null,
                List.of(new ConfigureExistingPropertyCommand.UnitTypePricing("Appartement", null)),
                List.of(new BuildingConfiguration("Bâtiment 1", 0, List.of(new UnitTypeConfiguration("Appartement", 3)))),
                List.of())))
                .isInstanceOf(PropertyConfigurationLimitExceededException.class);

        verify(buildingRepository, never()).save(any());
        verify(unitRepository, never()).save(any());
    }
}
