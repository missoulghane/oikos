package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.dto.MessageDraftView;
import com.architek.oikos.messaging.application.query.GetMessageDraftQuery;

public interface GetMessageDraftUseCase {

    MessageDraftView getDraft(GetMessageDraftQuery query);
}
