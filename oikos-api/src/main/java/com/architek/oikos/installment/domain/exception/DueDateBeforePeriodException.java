package com.architek.oikos.installment.domain.exception;

import java.time.LocalDate;
import java.time.YearMonth;

import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

/** A call cannot fall due before the period it bills has even started. */
public class DueDateBeforePeriodException extends BusinessException implements CodedException {

    public DueDateBeforePeriodException(LocalDate dueDate, YearMonth period) {
        super("Due date " + dueDate + " is before the start of period " + period);
    }

    @Override
    public String errorCode() {
        return ErrorCodes.DUE_DATE_BEFORE_PERIOD;
    }
}
