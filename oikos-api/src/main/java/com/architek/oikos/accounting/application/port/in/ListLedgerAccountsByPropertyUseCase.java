package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.LedgerAccountView;
import com.architek.oikos.accounting.application.query.ListLedgerAccountsByPropertyQuery;

public interface ListLedgerAccountsByPropertyUseCase {

    List<LedgerAccountView> list(ListLedgerAccountsByPropertyQuery query);
}
