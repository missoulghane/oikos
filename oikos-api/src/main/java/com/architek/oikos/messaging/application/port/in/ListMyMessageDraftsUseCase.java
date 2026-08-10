package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.dto.MessageDraftSummaryView;
import com.architek.oikos.messaging.application.query.ListMyMessageDraftsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListMyMessageDraftsUseCase {

    Page<MessageDraftSummaryView> listDrafts(ListMyMessageDraftsQuery query);
}
