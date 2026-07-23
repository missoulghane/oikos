package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;
import com.architek.oikos.installment.application.port.out.AccountLedgerPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.PropertyUnitPricingPort;
import com.architek.oikos.installment.application.port.out.UnitPriceLine;
import com.architek.oikos.installment.domain.exception.AccountNotFoundException;
import com.architek.oikos.installment.domain.exception.InstallmentCallAlreadyExistsException;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
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
    private AccountLedgerPort accountLedgerPort;

    @Mock
    private AutoAllocationEngine autoAllocationEngine;

    private GenerateInstallmentCallService newService() {
        return new GenerateInstallmentCallService(propertyDirectoryPort, propertyUnitPricingPort, installmentCallRepository,
                installmentRepository, accountLedgerPort, autoAllocationEngine);
    }

    private void stubHappyPath(EntityId propertyId) {
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(installmentCallRepository.existsByPropertyIdAndPeriod(any(), any())).thenReturn(false);
        when(accountLedgerPort.propertyAccountExists(propertyId)).thenReturn(true);
        when(installmentCallRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(installmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

        EntityId pricedUnitAccountId = EntityId.newId();
        when(accountLedgerPort.findUnitAccountId(pricedUnitId)).thenReturn(Optional.of(pricedUnitAccountId));

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));

        GenerateInstallmentCallResult result = newService().generate(command);

        assertThat(result.chargedUnitIds()).containsExactly(pricedUnitId);
        assertThat(result.skippedUnitIds()).containsExactly(unpricedUnitId);
        assertThat(result.installmentCall().propertyId()).isEqualTo(propertyId);
        assertThat(result.installmentCall().period()).isEqualTo(YearMonth.of(2026, 1));

        verify(accountLedgerPort).recordDebit(eq(pricedUnitAccountId), eq(new BigDecimal("300")), any());
        verify(autoAllocationEngine).allocate(pricedUnitAccountId);
        verify(accountLedgerPort, never()).findUnitAccountId(unpricedUnitId);
    }

    @Test
    void generating_for_a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void generating_twice_for_the_same_property_and_period_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(installmentCallRepository.existsByPropertyIdAndPeriod(propertyId, YearMonth.of(2026, 1))).thenReturn(true);

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(InstallmentCallAlreadyExistsException.class);
    }

    @Test
    void generating_without_a_property_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(installmentCallRepository.existsByPropertyIdAndPeriod(any(), any())).thenReturn(false);
        when(accountLedgerPort.propertyAccountExists(propertyId)).thenReturn(false);

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));

        assertThatThrownBy(() -> newService().generate(command)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void a_priced_unit_without_a_unit_account_yet_is_skipped_rather_than_aborting_the_batch() {
        EntityId propertyId = EntityId.newId();
        stubHappyPath(propertyId);

        EntityId pricedUnitId = EntityId.newId();
        EntityId unitWithoutAccountId = EntityId.newId();
        when(propertyUnitPricingPort.listUnitPrices(propertyId)).thenReturn(List.of(
                new UnitPriceLine(pricedUnitId, new BigDecimal("300")),
                new UnitPriceLine(unitWithoutAccountId, new BigDecimal("300"))));

        EntityId pricedUnitAccountId = EntityId.newId();
        when(accountLedgerPort.findUnitAccountId(pricedUnitId)).thenReturn(Optional.of(pricedUnitAccountId));
        when(accountLedgerPort.findUnitAccountId(unitWithoutAccountId)).thenReturn(Optional.empty());

        GenerateInstallmentCallCommand command = new GenerateInstallmentCallCommand(propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));

        GenerateInstallmentCallResult result = newService().generate(command);

        assertThat(result.chargedUnitIds()).containsExactly(pricedUnitId);
        assertThat(result.skippedUnitIds()).containsExactly(unitWithoutAccountId);
    }
}
