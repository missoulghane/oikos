package com.architek.oikos.messaging.web.response;

import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;

public record RecipientGroupReferenceResponse(String groupId) {

    public static RecipientGroupReferenceResponse from(RecipientGroupId id) {
        return new RecipientGroupReferenceResponse(id.toString());
    }
}
