package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.domain.exception.NothingToRegularizeException;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.UnitOfProperty;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RegularizeAdvancesServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);
    private static final EntityId PROPERTY_ID = EntityId.newId();

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;

    private RegularizeAdvancesService newService() {
        return new RegularizeAdvancesService(installmentRepository, regularizeUnitInstallmentsUseCase,
                Clock.fixed(Instant.parse("2026-10-28T02:30:00Z"), ZoneOffset.UTC));
    }

    private static RegularizeUnitInstallmentsResult result(EntityId unitId) {
        return new RegularizeUnitInstallmentsResult(unitId, EntityId.newId(), new BigDecimal("100"), List.of());
    }

    @Test
    void a_lot_is_regularized_on_demand_as_of_today_by_the_requesting_user() {
        EntityId unitId = EntityId.newId();
        EntityId userId = EntityId.newId();
        when(regularizeUnitInstallmentsUseCase.regularize(any())).thenReturn(result(unitId));

        newService().regularizeUnit(PROPERTY_ID, unitId, userId);

        verify(regularizeUnitInstallmentsUseCase)
                .regularize(new RegularizeUnitInstallmentsCommand(PROPERTY_ID, unitId, TODAY, userId));
    }

    @Test
    void the_sweep_tries_every_lot_owing_something_due_and_survives_failures() {
        EntityId regularized = EntityId.newId();
        EntityId withoutAdvance = EntityId.newId();
        EntityId failing = EntityId.newId();
        when(installmentRepository.findUnitsWithOutstandingDue(TODAY)).thenReturn(List.of(
                new UnitOfProperty(PROPERTY_ID, failing), new UnitOfProperty(PROPERTY_ID, withoutAdvance),
                new UnitOfProperty(PROPERTY_ID, regularized)));
        when(regularizeUnitInstallmentsUseCase.regularize(any())).thenAnswer(invocation -> {
            RegularizeUnitInstallmentsCommand command = invocation.getArgument(0);
            if (command.unitId().equals(failing)) {
                throw new IllegalStateException("no open exercise");
            }
            if (command.unitId().equals(withoutAdvance)) {
                throw new NothingToRegularizeException(withoutAdvance);
            }
            return result(regularized);
        });

        int count = newService().regularizeAllDue();

        assertThat(count).isEqualTo(1);
        ArgumentCaptor<RegularizeUnitInstallmentsCommand> commands =
                ArgumentCaptor.forClass(RegularizeUnitInstallmentsCommand.class);
        verify(regularizeUnitInstallmentsUseCase, times(3)).regularize(commands.capture());
        assertThat(commands.getAllValues()).allSatisfy(command -> {
            assertThat(command.pieceDate()).isEqualTo(TODAY);
            assertThat(command.createdByUserId()).isEqualTo(RegularizeAdvancesService.SYSTEM_ACTOR_ID);
        });
    }
}
