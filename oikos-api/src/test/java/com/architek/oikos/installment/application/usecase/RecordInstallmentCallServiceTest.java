package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.InstallmentCallLine;
import com.architek.oikos.installment.application.command.RecordInstallmentCallCommand;
import com.architek.oikos.installment.application.port.out.UnitAccountLedgerPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.domain.exception.UnitNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordInstallmentCallServiceTest {

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private UnitAccountLedgerPort unitAccountLedgerPort;

    private RecordInstallmentCallService newService() {
        return new RecordInstallmentCallService(installmentRepository, unitDirectoryPort, unitAccountLedgerPort);
    }

    @Test
    void recording_a_installment_call_creates_an_installment_per_line() {
        EntityId unitId = EntityId.newId();
        EntityId unitAccountId = EntityId.newId();
        when(unitDirectoryPort.exists(unitId)).thenReturn(true);
        when(installmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitAccountLedgerPort.findUnitAccountId(unitId)).thenReturn(Optional.of(unitAccountId));

        LocalDate dueDate = LocalDate.of(2027, 1, 1);
        List<InstallmentId> ids = newService().record(new RecordInstallmentCallCommand(dueDate,
                List.of(new InstallmentCallLine(unitId, new BigDecimal("250")))));

        assertThat(ids).hasSize(1);

        ArgumentCaptor<Installment> installmentCaptor = ArgumentCaptor.forClass(Installment.class);
        verify(installmentRepository).save(installmentCaptor.capture());
        Installment savedInstallment = installmentCaptor.getValue();
        assertThat(savedInstallment.getUnitId()).isEqualTo(unitId);
        assertThat(savedInstallment.getDueDate()).isEqualTo(dueDate);
        assertThat(savedInstallment.getAmount().value()).isEqualByComparingTo("250");

        verify(unitAccountLedgerPort).recordDebit(eq(unitAccountId), eq(new BigDecimal("250")), any(), any());
    }

    @Test
    void recording_a_installment_call_for_an_unknown_unit_is_rejected() {
        EntityId unitId = EntityId.newId();
        when(unitDirectoryPort.exists(unitId)).thenReturn(false);

        RecordInstallmentCallCommand command = new RecordInstallmentCallCommand(LocalDate.now(),
                List.of(new InstallmentCallLine(unitId, BigDecimal.TEN)));

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(UnitNotFoundException.class);
    }
}
