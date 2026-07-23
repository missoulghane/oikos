package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.AutoAllocateCommand;

/**
 * Exposes AutoAllocationEngine's FIFO matching as a cross-module capability
 * so accounting's RecordPaymentService can trigger it after recording a credit
 * movement (a payment may settle existing due installments), without
 * installment depending on accounting's repositories, and without accounting
 * depending on installment's internals beyond this one port-in (rule 4).
 */
public interface AutoAllocateUseCase {

    void allocate(AutoAllocateCommand command);
}
