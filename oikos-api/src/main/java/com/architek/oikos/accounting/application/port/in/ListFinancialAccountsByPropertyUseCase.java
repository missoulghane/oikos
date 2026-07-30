package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.FinancialAccountView;
import com.architek.oikos.accounting.application.query.ListFinancialAccountsByPropertyQuery;

public interface ListFinancialAccountsByPropertyUseCase {

    List<FinancialAccountView> list(ListFinancialAccountsByPropertyQuery query);
}
