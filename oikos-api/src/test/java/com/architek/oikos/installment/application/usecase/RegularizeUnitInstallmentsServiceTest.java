package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.port.out.AdvanceRegularizationPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.domain.exception.UnitNotFoundException;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RegularizeUnitInstallmentsServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private AdvanceRegularizationPort advanceRegularizationPort;

    @Test
    void a_unit_of_another_property_is_not_found_and_nothing_is_read_or_posted() {
        EntityId propertyId = EntityId.newId();
        EntityId foreignUnitId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(unitDirectoryPort.existsInProperty(foreignUnitId, propertyId)).thenReturn(false);
        RegularizeUnitInstallmentsService service = new RegularizeUnitInstallmentsService(propertyDirectoryPort,
                unitDirectoryPort, installmentRepository, advanceRegularizationPort);

        assertThatThrownBy(() -> service.regularize(new RegularizeUnitInstallmentsCommand(propertyId, foreignUnitId,
                LocalDate.of(2026, 10, 28), EntityId.newId()))).isInstanceOf(UnitNotFoundException.class);
        verifyNoInteractions(installmentRepository, advanceRegularizationPort);
    }
}
