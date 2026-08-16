package com.architek.oikos.meeting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.meeting.application.dto.AttendanceSummaryView;

public record AttendanceSummaryResponse(int totalUnits, int sentCount, int attendingCount, int notAttendingCount,
                                         int noReplyCount, int checkedInCount, BigDecimal totalWeight,
                                         BigDecimal presentWeight, BigDecimal quorumPercentage, boolean quorumRequired,
                                         boolean quorumReached) {

    public static AttendanceSummaryResponse from(AttendanceSummaryView view) {
        return new AttendanceSummaryResponse(view.totalUnits(), view.sentCount(), view.attendingCount(),
                view.notAttendingCount(), view.noReplyCount(), view.checkedInCount(), view.totalWeight(),
                view.presentWeight(), view.quorumPercentage(), view.quorumRequired(), view.quorumReached());
    }
}
