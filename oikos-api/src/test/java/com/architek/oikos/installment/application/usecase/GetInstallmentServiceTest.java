package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.query.GetInstallmentQuery;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetInstallmentServiceTest {

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private AllocationRepository allocationRepository;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2027-06-15T00:00:00Z"), ZoneOffset.UTC);

    private GetInstallmentService newService() {
        return new GetInstallmentService(installmentRepository, allocationRepository, FIXED_CLOCK);
    }

    @Test
    void an_installment_partially_paid_and_not_yet_due_is_reported_as_partially_paid() {
        Installment installment = Installment.create(InstallmentId.newId(), EntityId.newId(), EntityId.newId(),
                LocalDate.of(2027, 12, 1), Amount.of(new BigDecimal("250")));
        when(installmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));
        when(allocationRepository.sumAllocatedByInstallmentId(installment.getId())).thenReturn(new BigDecimal("100"));

        InstallmentView view = newService().getInstallment(new GetInstallmentQuery(installment.getId()));

        assertThat(view.amountPaid()).isEqualByComparingTo("100");
        assertThat(view.remainingDue()).isEqualByComparingTo("150");
        assertThat(view.status()).isEqualTo(InstallmentStatus.PARTIALLY_PAID);
    }

    @Test
    void an_installment_fully_allocated_is_reported_as_paid() {
        Installment installment = Installment.create(InstallmentId.newId(), EntityId.newId(), EntityId.newId(),
                LocalDate.of(2027, 1, 1), Amount.of(new BigDecimal("250")));
        when(installmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));
        when(allocationRepository.sumAllocatedByInstallmentId(installment.getId())).thenReturn(new BigDecimal("250"));

        InstallmentView view = newService().getInstallment(new GetInstallmentQuery(installment.getId()));

        assertThat(view.remainingDue()).isEqualByComparingTo("0");
        assertThat(view.status()).isEqualTo(InstallmentStatus.PAID);
    }
}
