package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.installment.application.dto.RecordOwnerPaymentResult;
import com.architek.oikos.installment.application.port.out.OwnerPaymentJournalEntryPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;
import com.architek.oikos.installment.domain.exception.UnitNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.repository.PaymentRepository;
import com.architek.oikos.installment.domain.repository.ReceiptNumberSequenceRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;
import com.architek.oikos.installment.domain.valueobject.ReceiptNumber;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordOwnerPaymentServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OwnerPaymentJournalEntryPort ownerPaymentJournalEntryPort;

    @Mock
    private ReceiptNumberSequenceRepository receiptNumberSequenceRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private RecordOwnerPaymentService newService() {
        return new RecordOwnerPaymentService(propertyDirectoryPort, unitDirectoryPort, installmentRepository,
                paymentRepository, ownerPaymentJournalEntryPort, receiptNumberSequenceRepository, eventPublisher);
    }

    private void stubDirectories(EntityId propertyId, EntityId unitId) {
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(unitDirectoryPort.exists(unitId)).thenReturn(true);
        when(receiptNumberSequenceRepository.allocate(any(), anyInt())).thenReturn(new ReceiptNumber(2026, 1));
    }

    @Test
    void P2_a_payment_fully_covered_by_unsettled_installments_is_imputed_with_no_advance() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        stubDirectories(propertyId, unitId);

        EntityId treasuryAccountId = EntityId.newId();
        Installment installment = Installment.create(InstallmentId.newId(), unitId, LocalDate.of(2026, 2, 5),
                Amount.of(new BigDecimal("300.00")));
        when(installmentRepository.findAllByUnitId(unitId)).thenReturn(List.of(installment));
        when(ownerPaymentJournalEntryPort.postOwnerPaymentEntry(eq(propertyId), eq(unitId), eq(treasuryAccountId),
                any(), any(), any(), any(), any(), any())).thenReturn(EntityId.newId());
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, PaymentMode.CASH,
                treasuryAccountId, LocalDate.of(2026, 2, 10), new BigDecimal("300.00"), EntityId.newId());

        RecordOwnerPaymentResult result = newService().record(command);

        assertThat(result.advanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().get(0).amount()).isEqualByComparingTo("300.00");

        ArgumentCaptor<Installment> savedCaptor = ArgumentCaptor.forClass(Installment.class);
        verify(installmentRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getOutstandingAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        ArgumentCaptor<BigDecimal> imputedCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> advanceCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(ownerPaymentJournalEntryPort).postOwnerPaymentEntry(eq(propertyId), eq(unitId), eq(treasuryAccountId),
                eq(LocalDate.of(2026, 2, 10)), imputedCaptor.capture(), advanceCaptor.capture(), any(), any(), any());
        assertThat(imputedCaptor.getValue()).isEqualByComparingTo("300.00");
        assertThat(advanceCaptor.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void P3_a_payment_exceeding_the_unsettled_total_leaves_the_remainder_as_an_advance() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        stubDirectories(propertyId, unitId);

        Installment installment = Installment.create(InstallmentId.newId(), unitId, LocalDate.of(2026, 2, 5),
                Amount.of(new BigDecimal("300.00")));
        when(installmentRepository.findAllByUnitId(unitId)).thenReturn(List.of(installment));
        when(ownerPaymentJournalEntryPort.postOwnerPaymentEntry(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(EntityId.newId());
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, PaymentMode.BANK_TRANSFER,
                EntityId.newId(), LocalDate.of(2026, 2, 10), new BigDecimal("500.00"), EntityId.newId());

        RecordOwnerPaymentResult result = newService().record(command);

        assertThat(result.advanceAmount()).isEqualByComparingTo("200.00");
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().get(0).amount()).isEqualByComparingTo("300.00");
    }

    @Test
    void P3_settles_the_oldest_unsettled_installment_first() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        stubDirectories(propertyId, unitId);

        Installment older = Installment.create(InstallmentId.newId(), unitId, LocalDate.of(2026, 1, 5),
                Amount.of(new BigDecimal("100.00")));
        Installment newer = Installment.create(InstallmentId.newId(), unitId, LocalDate.of(2026, 2, 5),
                Amount.of(new BigDecimal("100.00")));
        when(installmentRepository.findAllByUnitId(unitId)).thenReturn(List.of(newer, older));
        when(ownerPaymentJournalEntryPort.postOwnerPaymentEntry(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(EntityId.newId());
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, PaymentMode.CASH,
                EntityId.newId(), LocalDate.of(2026, 2, 10), new BigDecimal("100.00"), EntityId.newId());

        RecordOwnerPaymentResult result = newService().record(command);

        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().get(0).installmentId()).isEqualTo(EntityId.of(older.getId().asUuid()));
    }

    @Test
    void a_payment_for_a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, PaymentMode.CASH,
                EntityId.newId(), LocalDate.of(2026, 2, 10), new BigDecimal("100.00"), EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(PropertyNotFoundException.class);
        verifyNoInteractions(ownerPaymentJournalEntryPort, paymentRepository);
    }

    @Test
    void a_payment_for_a_missing_unit_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(unitDirectoryPort.exists(unitId)).thenReturn(false);

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, PaymentMode.CASH,
                EntityId.newId(), LocalDate.of(2026, 2, 10), new BigDecimal("100.00"), EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(UnitNotFoundException.class);
        verifyNoInteractions(ownerPaymentJournalEntryPort, paymentRepository);
    }

    @Test
    void the_saved_payment_carries_the_journal_entry_id_returned_by_the_port() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        stubDirectories(propertyId, unitId);
        when(installmentRepository.findAllByUnitId(unitId)).thenReturn(List.of());
        EntityId journalEntryId = EntityId.newId();
        when(ownerPaymentJournalEntryPort.postOwnerPaymentEntry(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(journalEntryId);
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, PaymentMode.CHECK,
                EntityId.newId(), LocalDate.of(2026, 2, 10), new BigDecimal("150.00"), EntityId.newId());

        newService().record(command);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getJournalEntryId()).isEqualTo(journalEntryId);
    }
}
