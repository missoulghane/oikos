package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.RegularizeDueInstallmentsCommand;
import com.architek.oikos.installment.application.command.RegularizePropertyInstallmentsCommand;
import com.architek.oikos.installment.application.dto.InstallmentAllocationView;
import com.architek.oikos.installment.application.dto.RegularizeDueInstallmentsResult;
import com.architek.oikos.installment.application.dto.RegularizePropertyInstallmentsResult;
import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;
import com.architek.oikos.installment.application.port.in.RegularizePropertyInstallmentsUseCase;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RegularizeDueInstallmentsServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private RegularizePropertyInstallmentsUseCase regularizePropertyInstallmentsUseCase;

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);
    private static final EntityId SYSTEM_USER = EntityId.newId();

    private RegularizeDueInstallmentsService newService() {
        return new RegularizeDueInstallmentsService(propertyDirectoryPort, regularizePropertyInstallmentsUseCase);
    }

    private RegularizePropertyInstallmentsResult oneUnitRegularized(String amount) {
        return new RegularizePropertyInstallmentsResult(
                List.of(new RegularizeUnitInstallmentsResult(EntityId.newId(), EntityId.newId(),
                        new BigDecimal(amount), List.of(new InstallmentAllocationView(EntityId.newId(),
                                new BigDecimal(amount))))),
                new BigDecimal(amount));
    }

    @Test
    void every_property_is_swept_on_the_run_date() {
        EntityId first = EntityId.newId();
        EntityId second = EntityId.newId();
        when(propertyDirectoryPort.listAllIds()).thenReturn(List.of(first, second));
        when(regularizePropertyInstallmentsUseCase.regularize(any())).thenReturn(oneUnitRegularized("500.00"));

        RegularizeDueInstallmentsResult result = newService()
                .regularize(new RegularizeDueInstallmentsCommand(TODAY, SYSTEM_USER));

        ArgumentCaptor<RegularizePropertyInstallmentsCommand> commands = ArgumentCaptor
                .forClass(RegularizePropertyInstallmentsCommand.class);
        verify(regularizePropertyInstallmentsUseCase, times(2)).regularize(commands.capture());
        assertThat(commands.getAllValues()).extracting(RegularizePropertyInstallmentsCommand::propertyId)
                .containsExactly(first, second);
        assertThat(commands.getAllValues()).allSatisfy(command -> assertThat(command.pieceDate())
                .as("the run date is the due-date cutoff: anything falling due later stays untouched")
                .isEqualTo(TODAY));
        assertThat(result.unitsRegularized()).isEqualTo(2);
        assertThat(result.totalAmountApplied()).isEqualByComparingTo("1000.00");
    }

    /**
     * One copropriété with a closed exercise, or an incomplete chart of accounts,
     * must not cost every other one its imputation for the night.
     */
    @Test
    void a_property_that_fails_does_not_stop_the_sweep() {
        EntityId failing = EntityId.newId();
        EntityId healthy = EntityId.newId();
        when(propertyDirectoryPort.listAllIds()).thenReturn(List.of(failing, healthy));
        when(regularizePropertyInstallmentsUseCase.regularize(any()))
                .thenThrow(new IllegalStateException("accounting exercise not open"))
                .thenReturn(oneUnitRegularized("300.00"));

        RegularizeDueInstallmentsResult result = newService()
                .regularize(new RegularizeDueInstallmentsCommand(TODAY, SYSTEM_USER));

        assertThat(result.propertiesVisited()).isEqualTo(2);
        assertThat(result.propertiesFailed()).isEqualTo(1);
        assertThat(result.unitsRegularized()).isEqualTo(1);
        assertThat(result.totalAmountApplied()).isEqualByComparingTo("300.00");
    }

    @Test
    void a_product_with_no_property_yet_is_a_no_op() {
        when(propertyDirectoryPort.listAllIds()).thenReturn(List.of());

        RegularizeDueInstallmentsResult result = newService()
                .regularize(new RegularizeDueInstallmentsCommand(TODAY, SYSTEM_USER));

        assertThat(result.propertiesVisited()).isZero();
        assertThat(result.totalAmountApplied()).isEqualByComparingTo("0");
    }
}
