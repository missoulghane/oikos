package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.LettrageProposalView;
import com.architek.oikos.accounting.application.query.GetUnitLettrageProposalQuery;

public interface GetUnitLettrageProposalUseCase {

    LettrageProposalView get(GetUnitLettrageProposalQuery query);
}
