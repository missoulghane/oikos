package com.architek.oikos.messaging.application.port.in;

import java.util.List;

import com.architek.oikos.messaging.application.dto.RecipientCandidateView;
import com.architek.oikos.messaging.application.query.ListRecipientCandidatesQuery;

public interface ListRecipientCandidatesUseCase {

    List<RecipientCandidateView> listCandidates(ListRecipientCandidatesQuery query);
}
