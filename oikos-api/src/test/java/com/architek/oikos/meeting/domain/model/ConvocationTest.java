package com.architek.oikos.meeting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class ConvocationTest {

    private static final Instant SENT_AT = Instant.parse("2026-08-20T09:00:00Z");
    private static final Instant REPLIED_AT = Instant.parse("2026-08-22T14:00:00Z");
    private static final Instant CHECKED_IN_AT = Instant.parse("2026-09-15T17:05:00Z");
    private static final ChannelCode POSTAL_MAIL = ChannelCode.of("POSTAL_MAIL");

    private static Convocation generated() {
        return Convocation.generate(ConvocationId.newId(), GeneralMeetingId.newId(), EntityId.newId(),
                VotingWeight.of(BigDecimal.valueOf(120)), "token-1", ShortCode.of("code01"));
    }

    private static ConvocationDelivery sentBy(ChannelCode channel, Instant at) {
        return ConvocationDelivery.sent(ConvocationDeliveryId.newId(), channel, at, null, null);
    }

    private static ConvocationDelivery failedOn(ChannelCode channel, Instant at) {
        return ConvocationDelivery.failed(ConvocationDeliveryId.newId(), channel, at, null);
    }

    private static Convocation sent() {
        return generated().recordDelivery(sentBy(ChannelCode.EMAIL, SENT_AT));
    }

    @Test
    void a_generated_convocation_is_pending_on_every_axis() {
        Convocation convocation = generated();

        assertThat(convocation.getDeliveries()).isEmpty();
        assertThat(convocation.getDeliveryStatus()).isEqualTo(DeliveryStatus.TO_SEND);
        assertThat(convocation.getAttendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
        assertThat(convocation.isCheckedIn()).isFalse();
        assertThat(convocation.status()).isEqualTo(ConvocationStatus.TO_SEND);
    }

    @Test
    void the_synthetic_status_follows_the_journey() {
        Convocation sent = sent();
        assertThat(sent.status()).isEqualTo(ConvocationStatus.SENT);

        Convocation confirmed = sent.reply(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, null, null, REPLIED_AT);
        assertThat(confirmed.status()).isEqualTo(ConvocationStatus.CONFIRMED);

        Convocation present = confirmed.checkIn(AttendanceMode.ON_SITE, null, CHECKED_IN_AT);
        assertThat(present.status()).isEqualTo(ConvocationStatus.CHECKED_IN);
    }

    @Test
    void declining_still_counts_as_having_answered() {
        Convocation declined = sent().reply(AttendanceReply.NOT_ATTENDING, ReplySource.SYNDIC_OFFICE, null,
                "appelée mardi", REPLIED_AT);

        assertThat(declined.status()).isEqualTo(ConvocationStatus.CONFIRMED);
        assertThat(declined.awaitsReply()).isFalse();
    }

    @Test
    void a_lot_that_turns_up_without_having_answered_is_still_present() {
        // Common in practice, and it must not fall back to SENT: the sign-in is what counts.
        Convocation present = sent().checkIn(AttendanceMode.ON_SITE, null, CHECKED_IN_AT);

        assertThat(present.getAttendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
        assertThat(present.status()).isEqualTo(ConvocationStatus.CHECKED_IN);
    }

    @Test
    void a_failed_delivery_carries_no_send_date() {
        // Leaving one would read as "sent, then failed" in the tracking table.
        Convocation failed = generated().recordDelivery(failedOn(ChannelCode.EMAIL, SENT_AT));

        assertThat(failed.getSentAt()).isNull();
        assertThat(failed.getDeliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(failed.status()).isEqualTo(ConvocationStatus.TO_SEND);
    }

    @Test
    void a_second_channel_is_added_to_the_first_and_never_replaces_it() {
        // The whole point of the change: an email then a registered letter is two facts, and
        // the previous shape kept only the last one.
        Instant later = SENT_AT.plusSeconds(86_400);
        Convocation twice = sent().recordDelivery(sentBy(POSTAL_MAIL, later));

        assertThat(twice.getDeliveries()).hasSize(2);
        assertThat(twice.getDeliveries()).extracting(delivery -> delivery.getChannelCode().value())
                .containsExactly("EMAIL", "POSTAL_MAIL");
    }

    @Test
    void one_successful_channel_is_enough_to_count_as_sent() {
        // A lot reached by post is reached, however badly the email went. What the syndic has
        // to see in the table is the lots nobody could reach at all.
        Convocation reached = generated().recordDelivery(failedOn(ChannelCode.EMAIL, SENT_AT))
                .recordDelivery(sentBy(POSTAL_MAIL, SENT_AT.plusSeconds(3600)));

        assertThat(reached.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(reached.status()).isEqualTo(ConvocationStatus.SENT);
    }

    @Test
    void the_send_date_is_the_first_success_not_the_last() {
        // The notice period runs from it, so a second channel must not push it back.
        Instant later = SENT_AT.plusSeconds(86_400);
        Convocation twice = sent().recordDelivery(sentBy(POSTAL_MAIL, later));

        assertThat(twice.getSentAt()).isEqualTo(SENT_AT);
    }

    @Test
    void a_delivery_row_cannot_carry_the_derived_pending_state() {
        // TO_SEND is the absence of any row, not one of their outcomes.
        assertThatThrownBy(() -> ConvocationDelivery.reconstruct(ConvocationDeliveryId.newId(), ChannelCode.EMAIL,
                DeliveryStatus.TO_SEND, null, null, null, SENT_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void only_a_delivered_and_still_silent_lot_awaits_a_reply() {
        assertThat(generated().awaitsReply()).isFalse();
        assertThat(generated().recordDelivery(failedOn(ChannelCode.EMAIL, SENT_AT)).awaitsReply()).isFalse();

        Convocation sent = sent();
        assertThat(sent.awaitsReply()).isTrue();

        assertThat(sent.reply(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, null, null, REPLIED_AT).awaitsReply())
                .isFalse();
    }

    @Test
    void answering_again_overwrites_the_previous_answer_and_its_provenance() {
        Instant later = REPLIED_AT.plusSeconds(3600);
        Convocation changed = sent()
                .reply(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, null, null, REPLIED_AT)
                .reply(AttendanceReply.NOT_ATTENDING, ReplySource.SYNDIC_OFFICE, null, "a rappelé le bureau", later);

        assertThat(changed.getAttendanceReply()).isEqualTo(AttendanceReply.NOT_ATTENDING);
        assertThat(changed.getRepliedAt()).isEqualTo(later);
        assertThat(changed.getReplySource()).isEqualTo(ReplySource.SYNDIC_OFFICE);
        assertThat(changed.getReplyNote()).isEqualTo("a rappelé le bureau");
    }

    @Test
    void an_answer_records_who_gave_it_when_it_is_known() {
        EntityId partyId = EntityId.newId();
        Convocation answered = sent().reply(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, partyId, null,
                REPLIED_AT);

        assertThat(answered.getRepliedByPartyId()).isEqualTo(partyId);
        assertThat(answered.getReplySource()).isEqualTo(ReplySource.OWNER_APP);
    }

    @Test
    void an_answer_withdrawn_back_to_no_reply_clears_its_whole_provenance() {
        Convocation withdrawn = generated()
                .reply(AttendanceReply.ATTENDING, ReplySource.OWNER_LINK, EntityId.newId(), "via le lien", REPLIED_AT)
                .reply(AttendanceReply.NO_REPLY, null, null, null, REPLIED_AT);

        assertThat(withdrawn.getRepliedAt()).isNull();
        assertThat(withdrawn.getReplySource()).isNull();
        assertThat(withdrawn.getRepliedByPartyId()).isNull();
        assertThat(withdrawn.getReplyNote()).isNull();
    }

    @Test
    void an_answer_without_a_source_cannot_exist() {
        // Mirrors the CHECK constraint on the table: knowing someone confirmed without knowing
        // how it was obtained is the gap this whole field exists to close.
        assertThatThrownBy(() -> sent().reply(AttendanceReply.ATTENDING, null, null, null, REPLIED_AT))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Convocation.reconstruct(ConvocationId.newId(), GeneralMeetingId.newId(),
                EntityId.newId(), VotingWeight.perUnit(), List.of(), "token-3", ShortCode.of("code02"), AttendanceReply.ATTENDING, REPLIED_AT,
                null, null, null, false, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_check_in_records_the_mode_and_optionally_who_represented_the_lot() {
        EntityId partyId = EntityId.newId();
        Convocation present = generated().checkIn(AttendanceMode.REMOTE, partyId, CHECKED_IN_AT);

        assertThat(present.getAttendanceMode()).isEqualTo(AttendanceMode.REMOTE);
        assertThat(present.getCheckedInPartyId()).isEqualTo(partyId);
        assertThat(present.getCheckedInAt()).isEqualTo(CHECKED_IN_AT);
    }

    @Test
    void undoing_a_check_in_clears_everything_it_recorded() {
        Convocation undone = generated().checkIn(AttendanceMode.ON_SITE, EntityId.newId(), CHECKED_IN_AT).undoCheckIn();

        assertThat(undone.isCheckedIn()).isFalse();
        assertThat(undone.getAttendanceMode()).isNull();
        assertThat(undone.getCheckedInPartyId()).isNull();
        assertThat(undone.getCheckedInAt()).isNull();
    }

    @Test
    void a_check_in_without_mode_or_timestamp_cannot_be_reconstructed() {
        assertThatThrownBy(() -> Convocation.reconstruct(ConvocationId.newId(), GeneralMeetingId.newId(),
                EntityId.newId(), VotingWeight.perUnit(), List.of(), "token-4", ShortCode.of("code03"), AttendanceReply.NO_REPLY, null, null,
                null, null, true, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_lot_with_no_tantiemes_assigned_yet_is_still_convokable() {
        // ConfigurePropertyService creates units with zero shares; refusing them here would
        // block the convocation of a whole copropriété over a data-entry gap.
        Convocation convocation = Convocation.generate(ConvocationId.newId(), GeneralMeetingId.newId(),
                EntityId.newId(), VotingWeight.of(BigDecimal.ZERO), "token-2", ShortCode.of("code04"));

        assertThat(convocation.getVotingWeight().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
