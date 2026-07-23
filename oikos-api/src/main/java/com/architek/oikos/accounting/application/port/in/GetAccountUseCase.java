package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.query.GetAccountQuery;

public interface GetAccountUseCase {

    AccountView getAccount(GetAccountQuery query);
}
