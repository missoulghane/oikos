package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;
import com.architek.oikos.installment.application.event.InstallmentCallGeneratedEvent;
import com.architek.oikos.installment.application.port.out.FundCallJournalEntryPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.PropertyDuesConfigurationView;
import com.architek.oikos.installment.application.port.out.PropertyUnitPricingPort;
import com.architek.oikos.installment.application.port.out.UnitPriceLine;
import com.architek.oikos.installment.application.port.out.UnitShareLine;
import com.architek.oikos.installment.domain.exception.DueDateBeforePeriodException;
import com.architek.oikos.installment.domain.exception.InstallmentCallAlreadyExistsException;
import com.architek.oikos.installment.domain.exception.NoChargeableUnitException;
import com.architek.oikos.installment.domain.exception.NoUnitSharesConfiguredException;
import com.architek.oikos.installment.domain.exception.ProjectedBudgetNotConfiguredException;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GenerateInstallmentCallServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private PropertyUnitPricingPort propertyUnitPricingPort;

    @Mock
    private InstallmentCallRepository installmentCallRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private FundCallJournalEntryPort fundCallJournalEntryPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private GenerateInstallmentCallService newService() {
        return new GenerateInstallmentCallService(propertyDirectoryPort, propertyUnitPricingPort, installmentCallRepository,
                installmentRepository, fundCallJournalEntryPort, eventPublisher);
    }

    private void stubHappyPath(EntityId propertyId) {
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(propertyDirectoryPort.getDuesConfiguration(propertyId))
                .thenReturn(new PropertyDuesConfigurationView(DuesCalculationMode.FLAT_RATE, null));
        when(installmentCallRepository.existsByPropertyIdAndPeriod(any(), any())).thenReturn(false);
        when(installmentCallRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(installmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(fundCallJournalEntryPort.postFundCallEntry(any(), any(), any(), any(), any())).thenReturn(EntityId.newId());
    }

    @Test
    void generating_a_call_charges_priced_units_and_skips_unpriced_ones() {
        EntityId propertyId = EntityId.newId();
        stubHappyPath(propertyId);

        EntityId pricedUnitId = EntityId.newId();
        EntityId unpricedUnitId = EntityId.newId();
        when(propertyUnitPricingPort.listUnitPrices(propertyId)).thenReturn(List.of(
                new UnitPriceLine(pricedUnitId, new BigDecimal("300")),
                new UnitPriceLine(unpricedUnitId, null)));

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        GenerateInstallmentCallResult result = newService().generate(command);

        assertThat(result.chargedUnitIds()).containsExactly(pricedUnitId);
        assertThat(result.skippedUnitIds()).containsExactly(unpricedUnitId);
        assertThat(result.installmentCall().propertyId()).isEqualTo(propertyId);
        assertThat(result.installmentCall().period()).isEqualTo(YearMonth.of(2026, 1));
    }

    @Test
    void P1_posts_the_fund_call_journal_entry_only_for_charged_units_dated_on_the_period() {
        EntityId propertyId = EntityId.newId();
        stubHappyPath(propertyId);

        EntityId pricedUnitId = EntityId.newId();
        EntityId unpricedUnitId = EntityId.newId();
        when(propertyUnitPricingPort.listUnitPrices(propertyId)).thenReturn(List.of(
                new UnitPriceLine(pricedUnitId, new BigDecimal("300")),
                new UnitPriceLine(unpricedUnitId, null)));

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        newService().generate(command);

        org.mockito.ArgumentCaptor<List<com.architek.oikos.installment.application.dto.FundCallLine>> linesCaptor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(fundCallJournalEntryPort).postFundCallEntry(org.mockito.ArgumentMatchers.eq(propertyId),
                org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 1, 1)), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), linesCaptor.capture());
        assertThat(linesCaptor.getValue()).extracting(com.architek.oikos.installment.application.dto.FundCallLine::unitId)
                .containsExactly(pricedUnitId);
    }

    @Test
    void a_call_with_no_chargeable_unit_is_refused_rather_than_saved_empty() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(propertyDirectoryPort.getDuesConfiguration(propertyId))
                .thenReturn(new PropertyDuesConfigurationView(DuesCalculationMode.FLAT_RATE, null));
        when(installmentCallRepository.existsByPropertyIdAndPeriod(any(), any())).thenReturn(false);
        when(propertyUnitPricingPort.listUnitPrices(propertyId)).thenReturn(List.of(
                new UnitPriceLine(EntityId.newId(), null)));

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(NoChargeableUnitException.class);
        org.mockito.Mockito.verify(installmentCallRepository, org.mockito.Mockito.never()).save(any());
        org.mockito.Mockito.verifyNoInteractions(fundCallJournalEntryPort);
    }

    @Test
    void a_due_date_before_the_billed_period_is_refused() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 3),
                LocalDate.of(2026, 2, 28), EntityId.newId());

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(DueDateBeforePeriodException.class);
    }

    @Test
    void a_due_date_after_the_exercise_end_still_posts_on_the_billed_period() {
        // Décembre payable en janvier : le produit reste rattaché à l'exercice de décembre.
        EntityId propertyId = EntityId.newId();
        stubHappyPath(propertyId);
        when(propertyUnitPricingPort.listUnitPrices(propertyId)).thenReturn(List.of(
                new UnitPriceLine(EntityId.newId(), new BigDecimal("300"))));

        newService().generate(new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 12),
                LocalDate.of(2027, 1, 5), EntityId.newId()));

        org.mockito.Mockito.verify(fundCallJournalEntryPort).postFundCallEntry(org.mockito.ArgumentMatchers.eq(propertyId),
                org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 12, 1)), any(), any(), any());
    }

    @Test
    void publishes_the_charged_units_for_advance_regularization() {
        EntityId propertyId = EntityId.newId();
        stubHappyPath(propertyId);
        EntityId pricedUnitId = EntityId.newId();
        EntityId userId = EntityId.newId();
        when(propertyUnitPricingPort.listUnitPrices(propertyId)).thenReturn(List.of(
                new UnitPriceLine(pricedUnitId, new BigDecimal("300"))));

        newService().generate(new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), userId));

        org.mockito.ArgumentCaptor<InstallmentCallGeneratedEvent> eventCaptor =
                org.mockito.ArgumentCaptor.forClass(InstallmentCallGeneratedEvent.class);
        org.mockito.Mockito.verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().chargedUnitIds()).containsExactly(pricedUnitId);
        assertThat(eventCaptor.getValue().generatedByUserId()).isEqualTo(userId);
    }

    @Test
    void generating_for_a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void generating_twice_for_the_same_property_and_period_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(installmentCallRepository.existsByPropertyIdAndPeriod(propertyId, YearMonth.of(2026, 1))).thenReturn(true);

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(InstallmentCallAlreadyExistsException.class);
    }

    @Test
    void generating_a_call_in_shares_mode_prorates_the_projected_budget_and_skips_zero_share_units() {
        EntityId propertyId = EntityId.newId();
        stubHappyPath(propertyId);
        when(propertyDirectoryPort.getDuesConfiguration(propertyId))
                .thenReturn(new PropertyDuesConfigurationView(DuesCalculationMode.SHARES, new BigDecimal("1000")));

        EntityId unitA = EntityId.newId();
        EntityId unitB = EntityId.newId();
        EntityId zeroShareUnit = EntityId.newId();
        // 300/900 and 600/900 of 1000 do not divide evenly (333.33.. / 666.66..) -
        // the last shared unit must absorb the rounding remainder.
        when(propertyUnitPricingPort.listUnitShares(propertyId)).thenReturn(List.of(
                new UnitShareLine(unitA, new BigDecimal("300")),
                new UnitShareLine(unitB, new BigDecimal("600")),
                new UnitShareLine(zeroShareUnit, BigDecimal.ZERO)));

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        GenerateInstallmentCallResult result = newService().generate(command);

        assertThat(result.chargedUnitIds()).containsExactlyInAnyOrder(unitA, unitB);
        assertThat(result.skippedUnitIds()).containsExactly(zeroShareUnit);
    }

    @Test
    void generating_a_call_in_shares_mode_without_a_projected_budget_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(installmentCallRepository.existsByPropertyIdAndPeriod(any(), any())).thenReturn(false);
        when(propertyDirectoryPort.getDuesConfiguration(propertyId))
                .thenReturn(new PropertyDuesConfigurationView(DuesCalculationMode.SHARES, null));

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(ProjectedBudgetNotConfiguredException.class);
    }

    @Test
    void generating_a_call_in_shares_mode_with_no_unit_shares_configured_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(installmentCallRepository.existsByPropertyIdAndPeriod(any(), any())).thenReturn(false);
        when(propertyDirectoryPort.getDuesConfiguration(propertyId))
                .thenReturn(new PropertyDuesConfigurationView(DuesCalculationMode.SHARES, new BigDecimal("1000")));
        when(propertyUnitPricingPort.listUnitShares(propertyId)).thenReturn(List.of(
                new UnitShareLine(EntityId.newId(), BigDecimal.ZERO)));

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5), EntityId.newId());

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(NoUnitSharesConfiguredException.class);
    }
}
