package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.installment.application.query.ListInstallmentsByPropertyQuery;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListInstallmentsByPropertyServiceTest {

    @Mock
    private PropertyUnitDirectoryPort propertyUnitDirectoryPort;

    @Mock
    private InstallmentRepository installmentRepository;

    private ListInstallmentsByPropertyService newService() {
        return new ListInstallmentsByPropertyService(propertyUnitDirectoryPort, installmentRepository);
    }

    @Test
    void returns_an_empty_page_without_querying_installments_when_the_property_has_no_unit() {
        EntityId propertyId = EntityId.newId();
        when(propertyUnitDirectoryPort.listUnitIds(propertyId)).thenReturn(List.of());

        Page<InstallmentView> page = newService().listInstallments(
                new ListInstallmentsByPropertyQuery(propertyId, InstallmentFilter.defaultFilter(), PageRequest.of(0, 20)));

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
        verify(installmentRepository, never()).findPageByUnitIds(any(), any(), any());
    }

    @Test
    void resolves_unit_ids_of_the_property_then_lists_their_installments() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        List<EntityId> unitIds = List.of(unitId);
        when(propertyUnitDirectoryPort.listUnitIds(propertyId)).thenReturn(unitIds);

        Installment installment = Installment.create(InstallmentId.newId(), unitId,
                LocalDate.of(2026, 8, 1), Amount.of(new BigDecimal("150")));
        InstallmentFilter filter = InstallmentFilter.defaultFilter();
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(installmentRepository.findPageByUnitIds(eq(unitIds), eq(filter), eq(pageRequest)))
                .thenReturn(Page.of(List.of(installment), 0, 20, 1));

        Page<InstallmentView> page = newService().listInstallments(new ListInstallmentsByPropertyQuery(propertyId, filter, pageRequest));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).id()).isEqualTo(installment.getId());
        assertThat(page.totalElements()).isEqualTo(1);
    }
}
