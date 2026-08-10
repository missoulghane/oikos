package com.architek.oikos.messaging.web.response;

import com.architek.oikos.messaging.application.dto.RecipientCandidateView;

public record RecipientCandidateResponse(String userId, String fullName, String roleLabel) {

    public static RecipientCandidateResponse from(RecipientCandidateView view) {
        return new RecipientCandidateResponse(view.userId().toString(), view.fullName(), view.roleLabel());
    }
}
