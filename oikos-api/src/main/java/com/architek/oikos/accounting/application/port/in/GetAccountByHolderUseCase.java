package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;

public interface GetAccountByHolderUseCase {

    AccountView getAccount(GetAccountByHolderQuery query);
}
