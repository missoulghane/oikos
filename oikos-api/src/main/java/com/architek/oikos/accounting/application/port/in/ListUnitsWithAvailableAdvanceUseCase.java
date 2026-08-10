package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.UnitAdvanceView;
import com.architek.oikos.accounting.application.query.ListUnitsWithAvailableAdvanceQuery;

public interface ListUnitsWithAvailableAdvanceUseCase {

    /** Every unit of this property carrying a strictly positive, un-imputed advance balance. */
    List<UnitAdvanceView> list(ListUnitsWithAvailableAdvanceQuery query);
}
