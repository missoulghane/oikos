package com.architek.oikos.accounting.application.port.in;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.query.GetUnitAdvanceBalanceQuery;

public interface GetUnitAdvanceBalanceUseCase {

    /** Net, un-imputed advance available for this unit (never negative). */
    BigDecimal get(GetUnitAdvanceBalanceQuery query);
}
