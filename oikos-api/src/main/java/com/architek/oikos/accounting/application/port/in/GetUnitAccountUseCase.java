package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.UnitAccountView;
import com.architek.oikos.accounting.application.query.GetUnitAccountQuery;

public interface GetUnitAccountUseCase {

    UnitAccountView get(GetUnitAccountQuery query);
}
