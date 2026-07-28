package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.dto.InstallmentCallDetailView;
import com.architek.oikos.installment.application.query.GetInstallmentCallQuery;
import com.architek.oikos.installment.domain.exception.InstallmentCallNotFoundException;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetInstallmentCallServiceTest {

    @Mock
    private InstallmentCallRepository installmentCallRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-01T00:00:00Z"), ZoneOffset.UTC);

    private GetInstallmentCallService newService() {
        return new GetInstallmentCallService(installmentCallRepository, installmentRepository, FIXED_CLOCK);
    }

    @Test
    void returns_the_installment_call_with_its_installments() {
        InstallmentCallId id = InstallmentCallId.newId();
        EntityId propertyId = EntityId.newId();
        InstallmentCall installmentCall = InstallmentCall.create(id, propertyId, YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5));
        when(installmentCallRepository.findById(id)).thenReturn(Optional.of(installmentCall));

        Installment installment = Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("300")), id);
        when(installmentRepository.findAllByInstallmentCallId(id)).thenReturn(List.of(installment));

        InstallmentCallDetailView view = newService().getInstallmentCall(new GetInstallmentCallQuery(id));

        assertThat(view.installmentCall().id()).isEqualTo(id);
        assertThat(view.installments()).hasSize(1);
        assertThat(view.installments().get(0).amount()).isEqualByComparingTo("300");
    }

    @Test
    void getting_a_missing_installment_call_throws() {
        InstallmentCallId id = InstallmentCallId.newId();
        when(installmentCallRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getInstallmentCall(new GetInstallmentCallQuery(id)))
                .isInstanceOf(InstallmentCallNotFoundException.class);
    }
}
