package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * unsettledDebits/unallocatedCredits: each movement's outstanding/available
 * amount right now, before this proposal is applied (spec: what the review
 * screen shows). lines: the proposed FIFO matches. totalUnmatchedDebit: fund
 * calls still left unpaid once every available credit is exhausted, after
 * applying lines. totalUnmatchedCredit: money left over once every
 * outstanding fund call is covered, after applying lines - an advance for a
 * future one.
 */
public record LettrageProposal(List<LettrageMovementLine> unsettledDebits, List<LettrageMovementLine> unallocatedCredits,
                                List<LettrageAllocationLine> lines, BigDecimal totalUnmatchedDebit,
                                BigDecimal totalUnmatchedCredit) {
}
