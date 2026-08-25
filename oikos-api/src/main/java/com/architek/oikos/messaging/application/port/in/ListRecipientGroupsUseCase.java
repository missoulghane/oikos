package com.architek.oikos.messaging.application.port.in;

import java.util.List;

import com.architek.oikos.messaging.application.dto.RecipientGroupView;
import com.architek.oikos.messaging.application.query.ListRecipientGroupsQuery;

public interface ListRecipientGroupsUseCase {

    List<RecipientGroupView> listGroups(ListRecipientGroupsQuery query);
}
