package com.architek.oikos.meeting.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.architek.oikos.meeting.domain.valueobject.MeetingType;

/** Zero is a legitimate value: it says "no quorum required", explicitly. */
public record SetMeetingQuorumSettingRequest(@NotNull MeetingType meetingType,
                                              @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal quorumPercentage) {
}
