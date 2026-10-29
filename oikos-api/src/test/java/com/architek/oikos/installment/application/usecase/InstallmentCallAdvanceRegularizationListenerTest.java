package com.architek.oikos.installment.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.event.InstallmentCallGeneratedEvent;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class InstallmentCallAdvanceRegularizationListenerTest {

    @Mock
    private RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;

    @Test
    void each_charged_lot_is_regularized_as_of_today_and_one_failure_does_not_stop_the_others() {
        EntityId propertyId = EntityId.newId();
        EntityId userId = EntityId.newId();
        EntityId failing = EntityId.newId();
        EntityId other = EntityId.newId();
        when(regularizeUnitInstallmentsUseCase.regularize(any())).thenThrow(new IllegalStateException("boom"))
                .thenReturn(null);
        InstallmentCallAdvanceRegularizationListener listener = new InstallmentCallAdvanceRegularizationListener(
                regularizeUnitInstallmentsUseCase, Clock.fixed(Instant.parse("2026-10-28T09:00:00Z"), ZoneOffset.UTC));

        listener.onInstallmentCallGenerated(new InstallmentCallGeneratedEvent(InstallmentCallId.newId(), propertyId,
                List.of(failing, other), userId));

        verify(regularizeUnitInstallmentsUseCase, times(2)).regularize(any());
        verify(regularizeUnitInstallmentsUseCase).regularize(
                new RegularizeUnitInstallmentsCommand(propertyId, other, LocalDate.of(2026, 10, 28), userId));
    }
}
