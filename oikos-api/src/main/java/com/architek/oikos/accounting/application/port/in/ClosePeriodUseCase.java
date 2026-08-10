package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.ClosePeriodCommand;
import com.architek.oikos.accounting.application.dto.PeriodView;

/**
 * P8 (spec &sect;6): closes a monthly period once every blocking check
 * (PeriodClosingValidator) passes and the previous period, if any, is
 * already closed - a period only closes in exercise order.
 */
public interface ClosePeriodUseCase {

    PeriodView close(ClosePeriodCommand command);
}
