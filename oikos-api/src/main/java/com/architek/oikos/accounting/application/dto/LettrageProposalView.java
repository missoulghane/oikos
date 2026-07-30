package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record LettrageProposalView(EntityId unitId, List<LettrageMovementView> unsettledDebits,
                                    List<LettrageMovementView> unallocatedCredits,
                                    List<LettrageProposalLineView> proposedLines, BigDecimal totalProposedAmount,
                                    BigDecimal totalUnmatchedDebit, BigDecimal totalUnmatchedCredit) {
}
