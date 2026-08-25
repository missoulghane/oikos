package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
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

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;
import com.architek.oikos.installment.application.event.PaymentRecordedEvent;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.domain.exception.NothingToRegularizeException;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class PaymentAdvanceRegularizationListenerTest {

    @Mock
    private RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;

    private static final EntityId PROPERTY_ID = EntityId.newId();
    private static final EntityId UNIT_ID = EntityId.newId();
    private static final EntityId RECORDING_USER = EntityId.newId();
    private static final LocalDate VALUE_DATE = LocalDate.of(2026, 3, 15);

    private PaymentRecordedEvent event() {
        return new PaymentRecordedEvent(PaymentId.newId(), PROPERTY_ID, UNIT_ID, VALUE_DATE, RECORDING_USER);
    }

    @Test
    void a_recorded_payment_regularizes_the_lot_on_the_payment_value_date() {
        when(regularizeUnitInstallmentsUseCase.regularize(any())).thenReturn(new RegularizeUnitInstallmentsResult(
                UNIT_ID, EntityId.newId(), new BigDecimal("1200.00"), List.of()));

        new PaymentAdvanceRegularizationListener(regularizeUnitInstallmentsUseCase).onPaymentRecorded(event());

        ArgumentCaptor<RegularizeUnitInstallmentsCommand> command = ArgumentCaptor
                .forClass(RegularizeUnitInstallmentsCommand.class);
        verify(regularizeUnitInstallmentsUseCase).regularize(command.capture());
        assertThat(command.getValue().propertyId()).isEqualTo(PROPERTY_ID);
        assertThat(command.getValue().unitId()).isEqualTo(UNIT_ID);
        assertThat(command.getValue().pieceDate())
                .as("the regularization must land in the same accounting period as the recette that triggered it")
                .isEqualTo(VALUE_DATE);
        assertThat(command.getValue().createdByUserId()).isEqualTo(RECORDING_USER);
    }

    /**
     * The ordinary outcome - most lots carry no advance - and the reason the
     * sweep exception is caught rather than left to surface: it would otherwise
     * be logged as a failure on every single payment.
     */
    @Test
    void a_lot_with_nothing_to_impute_is_not_an_error() {
        when(regularizeUnitInstallmentsUseCase.regularize(any()))
                .thenThrow(new NothingToRegularizeException(UNIT_ID));

        assertThatCode(
                () -> new PaymentAdvanceRegularizationListener(regularizeUnitInstallmentsUseCase)
                        .onPaymentRecorded(event()))
                .doesNotThrowAnyException();
    }

    /**
     * An encaissement that really happened cannot be lost because the
     * regularization behind it could not be posted - the payment is already
     * committed when this runs.
     */
    @Test
    void a_failing_regularization_never_propagates_out_of_the_listener() {
        when(regularizeUnitInstallmentsUseCase.regularize(any()))
                .thenThrow(new IllegalStateException("accounting period closed"));

        assertThatCode(
                () -> new PaymentAdvanceRegularizationListener(regularizeUnitInstallmentsUseCase)
                        .onPaymentRecorded(event()))
                .doesNotThrowAnyException();
    }
}
