package com.architek.oikos.meeting.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.model.ConvocationReply;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class AttendanceTallyTest {

    private static final GeneralMeetingId MEETING = GeneralMeetingId.newId();
    private static final Instant AT = Instant.parse("2026-09-15T17:00:00Z");

    private static Convocation lot(int weight) {
        return Convocation.generate(ConvocationId.newId(), MEETING, EntityId.newId(),
                VotingWeight.of(BigDecimal.valueOf(weight)), "token-1", ShortCode.of("code01"));
    }

    private static Convocation sent(Convocation convocation) {
        return convocation.recordDelivery(
                ConvocationDelivery.sent(ConvocationDeliveryId.newId(), ChannelCode.EMAIL, AT, null, null));
    }

    private static Convocation present(Convocation convocation) {
        return convocation.checkIn(AttendanceMode.ON_SITE, null, AT);
    }

    @Test
    void an_empty_meeting_tallies_to_nothing() {
        AttendanceTally tally = AttendanceTally.of(List.of());

        assertThat(tally.totalUnits()).isZero();
        assertThat(tally.totalWeight().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(tally.presentWeight().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void every_lot_counts_in_the_total_weight_whether_it_turned_up_or_not() {
        // What an ABSOLUTE majority is measured against, and the denominator of the quorum.
        AttendanceTally tally = AttendanceTally.of(List.of(present(lot(100)), sent(lot(200)), lot(300)));

        assertThat(tally.totalUnits()).isEqualTo(3);
        assertThat(tally.totalWeight().value()).isEqualByComparingTo(BigDecimal.valueOf(600));
        assertThat(tally.presentWeight().value()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void announcing_attendance_does_not_make_a_lot_present() {
        // An owner who confirms and then does not come cannot make the quorum.
        AttendanceTally tally = AttendanceTally
                .of(List.of(sent(lot(500)).reply(ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.ATTENDING, null, false, ReplySource.OWNER_APP, null, null, null, AT, null)), sent(lot(500))));

        assertThat(tally.attendingCount()).isEqualTo(1);
        assertThat(tally.checkedInCount()).isZero();
        assertThat(tally.presentWeight().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void the_reply_counters_partition_the_lots() {
        AttendanceTally tally = AttendanceTally.of(List.of(
                sent(lot(10)).reply(ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.ATTENDING, null, false, ReplySource.OWNER_APP, null, null, null, AT, null)),
                sent(lot(10)).reply(ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.NOT_ATTENDING, null, false, ReplySource.OWNER_APP, null, null, null, AT, null)),
                sent(lot(10)),
                lot(10)));

        assertThat(tally.attendingCount()).isEqualTo(1);
        assertThat(tally.notAttendingCount()).isEqualTo(1);
        assertThat(tally.noReplyCount()).isEqualTo(2);
        assertThat(tally.sentCount()).isEqualTo(3);
    }

    @Test
    void the_quorum_is_decided_on_present_weight_over_total_weight() {
        AttendanceTally tally = AttendanceTally.of(List.of(present(lot(600)), lot(400)));

        assertThat(tally.isQuorumReachedFor(QuorumPercentage.of(BigDecimal.valueOf(50)))).isTrue();
        assertThat(tally.isQuorumReachedFor(QuorumPercentage.of(BigDecimal.valueOf(60)))).isTrue();
        assertThat(tally.isQuorumReachedFor(QuorumPercentage.of(BigDecimal.valueOf(75)))).isFalse();
    }

    @Test
    void one_voice_per_lot_and_tantiemes_are_the_same_computation_on_different_weights() {
        AttendanceTally perUnit = AttendanceTally.of(List.of(present(lot(1)), lot(1), lot(1)));

        assertThat(perUnit.isQuorumReachedFor(QuorumPercentage.of(BigDecimal.valueOf(33)))).isTrue();
        assertThat(perUnit.isQuorumReachedFor(QuorumPercentage.of(BigDecimal.valueOf(34)))).isFalse();
    }

    @Test
    void an_unconfigured_quorum_is_reached_by_anything_including_an_empty_room() {
        AttendanceTally tally = AttendanceTally.of(List.of(lot(100), lot(200)));

        assertThat(tally.isQuorumReachedFor(QuorumPercentage.none())).isTrue();
    }
}
