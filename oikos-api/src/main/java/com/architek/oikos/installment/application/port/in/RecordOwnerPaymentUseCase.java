package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.installment.application.dto.RecordOwnerPaymentResult;

/**
 * Records an owner payment (P2, spec &sect;4.1), splits it FIFO between the
 * unit's unsettled fund calls and an advance (P3, spec &sect;4.2 -
 * PaymentAllocationCalculator), posts the resulting treasury journal entry,
 * and keeps each settled Installment's outstandingAmount in sync.
 */
public interface RecordOwnerPaymentUseCase {

    RecordOwnerPaymentResult record(RecordOwnerPaymentCommand command);
}
