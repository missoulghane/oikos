package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.LettrageProposalLineView;

public record LettrageProposalLineResponse(String debitMovementId, String creditMovementId, BigDecimal amount) {

    public static LettrageProposalLineResponse from(LettrageProposalLineView view) {
        return new LettrageProposalLineResponse(view.debitMovementId().toString(), view.creditMovementId().toString(),
                view.amount());
    }
}
