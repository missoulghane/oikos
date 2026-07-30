package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.TreasurySummaryView;
import com.architek.oikos.accounting.application.query.GetTreasurySummaryQuery;

public interface GetTreasurySummaryUseCase {

    TreasurySummaryView get(GetTreasurySummaryQuery query);
}
