package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.domain.exception.InstallmentNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class UpdateInstallmentSettlementServiceTest {

    @Mock
    private InstallmentRepository installmentRepository;

    private UpdateInstallmentSettlementService newService() {
        return new UpdateInstallmentSettlementService(installmentRepository);
    }

    @Test
    void updates_the_outstanding_amount_of_the_matching_installment() {
        Installment installment = Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 3, 1), Amount.of(new BigDecimal("300")));
        when(installmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));
        when(installmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().update(EntityId.of(installment.getId().asUuid()), new BigDecimal("150"));

        ArgumentCaptor<Installment> captor = ArgumentCaptor.forClass(Installment.class);
        verify(installmentRepository).save(captor.capture());
        assertThat(captor.getValue().getOutstandingAmount()).isEqualByComparingTo("150");
    }

    @Test
    void updating_an_unknown_installment_is_rejected() {
        EntityId unknownId = EntityId.newId();
        when(installmentRepository.findById(InstallmentId.of(unknownId.value()))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().update(unknownId, BigDecimal.ZERO))
                .isInstanceOf(InstallmentNotFoundException.class);
    }
}
