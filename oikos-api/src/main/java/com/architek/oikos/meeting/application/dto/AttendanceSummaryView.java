package com.architek.oikos.meeting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.meeting.domain.valueobject.AttendanceTally;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;

/**
 * The head of the tracking screen, and what the syndic looks at before opening
 * the session: sent / confirmed / present, then the two weights the quorum is
 * decided on.
 *
 * <p>quorumRequired distinguishes a copropriété that requires no quorum from
 * one whose threshold was simply never configured - both read as zero, and
 * only the second is a gap to fill.
 */
public record AttendanceSummaryView(int totalUnits, int sentCount, int attendingCount, int notAttendingCount,
                                     int noReplyCount, int checkedInCount, BigDecimal totalWeight,
                                     BigDecimal presentWeight, BigDecimal quorumPercentage, boolean quorumRequired,
                                     boolean quorumReached) {

    public static AttendanceSummaryView from(AttendanceTally tally, QuorumPercentage quorum) {
        return new AttendanceSummaryView(tally.totalUnits(), tally.sentCount(), tally.attendingCount(),
                tally.notAttendingCount(), tally.noReplyCount(), tally.checkedInCount(), tally.totalWeight().value(),
                tally.presentWeight().value(), quorum.value(), quorum.isRequired(), tally.isQuorumReachedFor(quorum));
    }
}
