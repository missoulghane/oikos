package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.CancelInstallmentCallCommand;
import com.architek.oikos.installment.application.port.out.FundCallJournalEntryPort;
import com.architek.oikos.installment.domain.exception.InstallmentCallNotFoundException;
import com.architek.oikos.installment.domain.exception.InstallmentCallSettledException;
import com.architek.oikos.installment.domain.exception.InvalidInstallmentCallTransitionException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallStatus;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CancelInstallmentCallServiceTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();
    private static final EntityId USER_ID = EntityId.newId();
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);

    @Mock
    private InstallmentCallRepository installmentCallRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private FundCallJournalEntryPort fundCallJournalEntryPort;

    private CancelInstallmentCallService newService() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-28T09:00:00Z"), ZoneOffset.UTC);
        return new CancelInstallmentCallService(installmentCallRepository, installmentRepository,
                fundCallJournalEntryPort, clock);
    }

    private static InstallmentCall posted(EntityId journalEntryId) {
        return InstallmentCall.draft(InstallmentCallId.newId(), PROPERTY_ID, YearMonth.of(2026, 10),
                LocalDate.of(2026, 10, 5)).issue().post(journalEntryId);
    }

    private static Installment installment(InstallmentCallId callId, String outstanding) {
        Installment created = Installment.create(InstallmentId.newId(), EntityId.newId(), LocalDate.of(2026, 10, 5),
                Amount.of(new BigDecimal("300")), callId);
        return created.withOutstandingAmount(new BigDecimal(outstanding));
    }

    @Test
    void a_posted_call_is_reversed_kept_as_cancelled_and_loses_its_installments() {
        EntityId journalEntryId = EntityId.newId();
        InstallmentCall call = posted(journalEntryId);
        when(installmentCallRepository.findById(call.getId())).thenReturn(Optional.of(call));
        when(installmentRepository.findAllByInstallmentCallId(call.getId()))
                .thenReturn(List.of(installment(call.getId(), "300"), installment(call.getId(), "300")));

        newService().cancel(new CancelInstallmentCallCommand(call.getId(), USER_ID));

        verify(fundCallJournalEntryPort).reverseFundCallEntry(PROPERTY_ID, journalEntryId, TODAY, USER_ID);
        verify(installmentRepository).deleteAllByInstallmentCallId(call.getId());
        ArgumentCaptor<InstallmentCall> saved = ArgumentCaptor.forClass(InstallmentCall.class);
        verify(installmentCallRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(InstallmentCallStatus.CANCELLED);
        assertThat(saved.getValue().getJournalEntryId()).contains(journalEntryId);
    }

    @Test
    void a_call_with_a_partly_settled_installment_cannot_be_cancelled() {
        InstallmentCall call = posted(EntityId.newId());
        when(installmentCallRepository.findById(call.getId())).thenReturn(Optional.of(call));
        when(installmentRepository.findAllByInstallmentCallId(call.getId()))
                .thenReturn(List.of(installment(call.getId(), "300"), installment(call.getId(), "120")));

        assertThatThrownBy(() -> newService().cancel(new CancelInstallmentCallCommand(call.getId(), USER_ID)))
                .isInstanceOf(InstallmentCallSettledException.class);
        verifyNoInteractions(fundCallJournalEntryPort);
        verify(installmentRepository, never()).deleteAllByInstallmentCallId(any());
        verify(installmentCallRepository, never()).save(any());
    }

    @Test
    void a_cancelled_call_cannot_be_cancelled_again() {
        InstallmentCall cancelled = posted(EntityId.newId()).cancelPosted();
        when(installmentCallRepository.findById(cancelled.getId())).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> newService().cancel(new CancelInstallmentCallCommand(cancelled.getId(), USER_ID)))
                .isInstanceOf(InvalidInstallmentCallTransitionException.class);
        verifyNoInteractions(fundCallJournalEntryPort);
    }

    @Test
    void an_unknown_call_is_not_found() {
        InstallmentCallId id = InstallmentCallId.newId();
        when(installmentCallRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().cancel(new CancelInstallmentCallCommand(id, USER_ID)))
                .isInstanceOf(InstallmentCallNotFoundException.class);
    }
}
