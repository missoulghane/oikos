package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.ReopenPeriodCommand;
import com.architek.oikos.accounting.application.dto.PeriodView;

public interface ReopenPeriodUseCase {

    PeriodView reopen(ReopenPeriodCommand command);
}
