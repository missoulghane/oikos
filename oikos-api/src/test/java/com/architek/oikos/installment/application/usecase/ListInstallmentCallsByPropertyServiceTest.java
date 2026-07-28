package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.dto.InstallmentCallSummaryView;
import com.architek.oikos.installment.application.query.ListInstallmentCallsByPropertyQuery;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListInstallmentCallsByPropertyServiceTest {

    @Mock
    private InstallmentCallRepository installmentCallRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @Test
    void lists_installment_calls_with_their_unit_count_and_total_amount() {
        EntityId propertyId = EntityId.newId();
        InstallmentCallId installmentCallId = InstallmentCallId.newId();
        InstallmentCall installmentCall = InstallmentCall.create(installmentCallId, propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));
        when(installmentCallRepository.findPageByPropertyId(propertyId, PageRequest.of(0, 20)))
                .thenReturn(Page.of(List.of(installmentCall), 0, 20, 1));

        Installment installmentA = Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("300")), installmentCallId);
        Installment installmentB = Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("100")), installmentCallId);
        when(installmentRepository.findAllByInstallmentCallId(installmentCallId)).thenReturn(List.of(installmentA, installmentB));

        Page<InstallmentCallSummaryView> page = new ListInstallmentCallsByPropertyService(installmentCallRepository, installmentRepository)
                .listInstallmentCalls(new ListInstallmentCallsByPropertyQuery(propertyId, PageRequest.of(0, 20)));

        assertThat(page.content()).hasSize(1);
        InstallmentCallSummaryView summary = page.content().get(0);
        assertThat(summary.unitCount()).isEqualTo(2);
        assertThat(summary.totalAmount()).isEqualByComparingTo("400");
    }
}
