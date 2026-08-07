package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.DeleteInstallmentCallCommand;
import com.architek.oikos.installment.domain.exception.InstallmentCallNotFoundException;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class DeleteInstallmentCallServiceTest {

    @Mock
    private InstallmentCallRepository installmentCallRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    private DeleteInstallmentCallService newService() {
        return new DeleteInstallmentCallService(installmentCallRepository, installmentRepository);
    }

    @Test
    void deleting_a_call_removes_its_installments_then_the_call_itself() {
        InstallmentCallId id = InstallmentCallId.newId();
        InstallmentCall installmentCall = InstallmentCall.create(id, EntityId.newId(), YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));
        when(installmentCallRepository.findById(id)).thenReturn(Optional.of(installmentCall));

        newService().delete(new DeleteInstallmentCallCommand(id));

        InOrder order = Mockito.inOrder(installmentRepository, installmentCallRepository);
        order.verify(installmentRepository).deleteAllByInstallmentCallId(id);
        order.verify(installmentCallRepository).deleteById(id);
    }

    @Test
    void deleting_a_missing_call_throws_and_deletes_nothing() {
        InstallmentCallId id = InstallmentCallId.newId();
        when(installmentCallRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().delete(new DeleteInstallmentCallCommand(id)))
                .isInstanceOf(InstallmentCallNotFoundException.class);

        verify(installmentRepository, never()).deleteAllByInstallmentCallId(id);
        verify(installmentCallRepository, never()).deleteById(id);
    }
}
