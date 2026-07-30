package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.PendingLettrageView;
import com.architek.oikos.accounting.application.query.ListPendingLettragesByPropertyQuery;

public interface ListPendingLettragesByPropertyUseCase {

    List<PendingLettrageView> list(ListPendingLettragesByPropertyQuery query);
}
