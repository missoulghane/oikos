package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.accounting.application.dto.LettrageProposalView;

public record LettrageProposalResponse(String unitId, List<LettrageMovementResponse> unsettledDebits,
                                        List<LettrageMovementResponse> unallocatedCredits,
                                        List<LettrageProposalLineResponse> proposedLines,
                                        BigDecimal totalProposedAmount, BigDecimal totalUnmatchedDebit,
                                        BigDecimal totalUnmatchedCredit) {

    public static LettrageProposalResponse from(LettrageProposalView view) {
        return new LettrageProposalResponse(view.unitId().toString(),
                view.unsettledDebits().stream().map(LettrageMovementResponse::from).toList(),
                view.unallocatedCredits().stream().map(LettrageMovementResponse::from).toList(),
                view.proposedLines().stream().map(LettrageProposalLineResponse::from).toList(),
                view.totalProposedAmount(), view.totalUnmatchedDebit(), view.totalUnmatchedCredit());
    }
}
