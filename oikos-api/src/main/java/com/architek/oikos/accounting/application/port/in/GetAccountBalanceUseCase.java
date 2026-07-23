package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.BalanceView;
import com.architek.oikos.accounting.application.query.GetAccountBalanceQuery;

public interface GetAccountBalanceUseCase {

    BalanceView getBalance(GetAccountBalanceQuery query);
}
