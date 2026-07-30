package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.UnitAccountSummaryView;
import com.architek.oikos.accounting.application.query.ListUnitAccountSummariesByPropertyQuery;

public interface ListUnitAccountSummariesByPropertyUseCase {

    List<UnitAccountSummaryView> list(ListUnitAccountSummariesByPropertyQuery query);
}
