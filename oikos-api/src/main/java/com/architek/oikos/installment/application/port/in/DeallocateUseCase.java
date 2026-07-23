package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.DeallocateCommand;

public interface DeallocateUseCase {

    void deallocate(DeallocateCommand command);
}
