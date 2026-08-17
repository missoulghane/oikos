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
import com.architek.oikos.meeting.domain.valueobject.ConvocationReplyId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
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

    private static ConvocationReply answer(AttendanceReply reply, ReplySource source, Instant receivedAt) {
        return ConvocationReply.record(ConvocationReplyId.newId(), reply, null, false, source, null, null, null,
                receivedAt, null);
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

        Convocation confirmed = sent.reply(answer(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, REPLIED_AT));
        assertThat(confirmed.status()).isEqualTo(ConvocationStatus.CONFIRMED);

        Convocation present = confirmed.checkIn(AttendanceMode.ON_SITE, null, CHECKED_IN_AT);
        assertThat(present.status()).isEqualTo(ConvocationStatus.CHECKED_IN);
    }

    @Test
    void declining_still_counts_as_having_answered() {
        Convocation declined = sent().reply(ConvocationReply.record(ConvocationReplyId.newId(),
                AttendanceReply.NOT_ATTENDING, null, false, ReplySource.OTHER, ReplyMediumCode.of("TELEPHONE"), null,
                "appelée mardi", REPLIED_AT, null));

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
    void a_channel_that_worked_is_only_ever_sent_on_that_channel() {
        // What each of the syndic's per-channel buttons selects on. Asking the convocation's
        // overall status instead would let a successful email run empty every other channel's
        // population - press "Email", and "Messagerie" then finds nobody left to send to.
        Convocation emailed = sent();

        assertThat(emailed.hasBeenSentBy(ChannelCode.EMAIL)).isTrue();
        assertThat(emailed.hasBeenSentBy(ChannelCode.APP)).isFalse();
        assertThat(emailed.hasBeenSentBy(POSTAL_MAIL)).isFalse();
    }

    @Test
    void a_failed_attempt_leaves_its_channel_still_to_send() {
        Convocation bounced = generated().recordDelivery(failedOn(ChannelCode.EMAIL, SENT_AT));

        assertThat(bounced.hasBeenSentBy(ChannelCode.EMAIL)).isFalse();
    }

    @Test
    void a_reminder_is_recorded_like_any_send_but_never_moves_the_notice_date() {
        // The date a contested AG turns on. A chase sent a week later is a real delivery row -
        // it just must not become the date the convocation is deemed to have gone out.
        Instant later = SENT_AT.plusSeconds(604_800);
        Convocation chased = sent().recordDelivery(ConvocationDelivery.reminderSent(ConvocationDeliveryId.newId(),
                ChannelCode.EMAIL, later, null));

        assertThat(chased.getDeliveries()).hasSize(2);
        assertThat(chased.getDeliveries().get(1).isReminder()).isTrue();
        assertThat(chased.getSentAt()).isEqualTo(SENT_AT);
        assertThat(chased.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
    }

    @Test
    void a_reminder_cannot_carry_a_tracking_reference() {
        // A tracking number belongs to a registered letter, recorded by hand; a reminder goes
        // out on an automated channel. The two together mean the paths were confused upstream.
        assertThatThrownBy(() -> ConvocationDelivery.reconstruct(ConvocationDeliveryId.newId(), ChannelCode.EMAIL,
                DeliveryStatus.SENT, SENT_AT, "RR-12345", true, null, SENT_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_delivery_row_cannot_carry_the_derived_pending_state() {
        // TO_SEND is the absence of any row, not one of their outcomes.
        assertThatThrownBy(() -> ConvocationDelivery.reconstruct(ConvocationDeliveryId.newId(), ChannelCode.EMAIL,
                DeliveryStatus.TO_SEND, null, null, false, null, SENT_AT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void only_a_delivered_and_still_silent_lot_awaits_a_reply() {
        assertThat(generated().awaitsReply()).isFalse();
        assertThat(generated().recordDelivery(failedOn(ChannelCode.EMAIL, SENT_AT)).awaitsReply()).isFalse();

        Convocation sent = sent();
        assertThat(sent.awaitsReply()).isTrue();

        assertThat(sent.reply(answer(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, REPLIED_AT)).awaitsReply())
                .isFalse();
    }

    @Test
    void answering_again_moves_the_standing_answer_and_keeps_the_previous_one() {
        Instant later = REPLIED_AT.plusSeconds(3600);
        Convocation changed = sent()
                .reply(answer(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, REPLIED_AT))
                .reply(ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.NOT_ATTENDING, null, false,
                        ReplySource.OTHER, ReplyMediumCode.of("TELEPHONE"), null, "a rappelé le bureau", later, null));

        assertThat(changed.getAttendanceReply()).isEqualTo(AttendanceReply.NOT_ATTENDING);
        assertThat(changed.getRepliedAt()).isEqualTo(later);
        assertThat(changed.getReplySource()).isEqualTo(ReplySource.OTHER);
        assertThat(changed.getReplyMedium()).isEqualTo(ReplyMediumCode.of("TELEPHONE"));
        assertThat(changed.getReplyNote()).isEqualTo("a rappelé le bureau");
        // The point of the history: the first answer is still there to be shown.
        assertThat(changed.getReplies()).hasSize(2);
        assertThat(changed.getReplies()).extracting(ConvocationReply::getReply)
                .containsExactly(AttendanceReply.ATTENDING, AttendanceReply.NOT_ATTENDING);
    }

    @Test
    void the_standing_answer_is_always_the_head_of_the_history() {
        // The invariant the whole denormalisation rests on. Convocation.reply() is the only
        // way to touch either half, so the two can never be made to disagree.
        Convocation changed = sent()
                .reply(answer(AttendanceReply.ATTENDING, ReplySource.OWNER_APP, REPLIED_AT))
                .reply(answer(AttendanceReply.NOT_ATTENDING, ReplySource.OWNER_LINK, REPLIED_AT.plusSeconds(3600)));

        ConvocationReply standing = changed.getReplies().stream().min(ConvocationReply.LATEST_FIRST).orElseThrow();
        assertThat(changed.getAttendanceReply()).isEqualTo(standing.getReply());
        assertThat(changed.getRepliedAt()).isEqualTo(standing.getReceivedAt());
        assertThat(changed.getReplySource()).isEqualTo(standing.getSource());
    }

    @Test
    void an_answer_backdated_before_the_standing_one_is_filed_and_changes_nothing() {
        // A letter that arrived on Tuesday, keyed in on Thursday, after a phone call on
        // Wednesday already superseded it. Recording it must stay safe.
        Instant wednesday = REPLIED_AT.plusSeconds(86_400);
        Convocation convocation = sent()
                .reply(answer(AttendanceReply.NOT_ATTENDING, ReplySource.OWNER_APP, wednesday))
                .reply(answer(AttendanceReply.ATTENDING, ReplySource.OWNER_LINK, REPLIED_AT));

        assertThat(convocation.getReplies()).hasSize(2);
        assertThat(convocation.getAttendanceReply()).isEqualTo(AttendanceReply.NOT_ATTENDING);
        assertThat(convocation.getRepliedAt()).isEqualTo(wednesday);
    }

    @Test
    void an_attending_answer_carries_what_it_announces() {
        Convocation announced = sent().reply(ConvocationReply.record(ConvocationReplyId.newId(),
                AttendanceReply.ATTENDING, AttendanceMode.REMOTE, true, ReplySource.OWNER_APP, null, null, null,
                REPLIED_AT, null));

        assertThat(announced.getReplyAttendanceMode()).isEqualTo(AttendanceMode.REMOTE);
        assertThat(announced.isReplyByProxy()).isTrue();
        // Announced, never constated: it grants no presence, and the check-in is untouched.
        assertThat(announced.isCheckedIn()).isFalse();
        assertThat(announced.getAttendanceMode()).isNull();
        assertThat(announced.status()).isEqualTo(ConvocationStatus.CONFIRMED);
    }

    @Test
    void an_absent_answer_cannot_announce_how_it_would_attend() {
        // "Absent, sur place" and "absent par procuration" are not states of the world.
        assertThatThrownBy(() -> ConvocationReply.record(ConvocationReplyId.newId(),
                AttendanceReply.NOT_ATTENDING, AttendanceMode.ON_SITE, false, ReplySource.OWNER_APP, null, null, null,
                REPLIED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ConvocationReply.record(ConvocationReplyId.newId(),
                AttendanceReply.NOT_ATTENDING, null, true, ReplySource.OWNER_APP, null, null, null, REPLIED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withdrawing_clears_what_the_previous_answer_announced() {
        Convocation withdrawn = sent()
                .reply(ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.ATTENDING,
                        AttendanceMode.ON_SITE, true, ReplySource.OWNER_APP, null, null, null, REPLIED_AT, null))
                .reply(answer(AttendanceReply.NO_REPLY, ReplySource.OWNER_APP, REPLIED_AT.plusSeconds(60)));

        assertThat(withdrawn.getReplyAttendanceMode()).isNull();
        assertThat(withdrawn.isReplyByProxy()).isFalse();
    }

    @Test
    void a_medium_cannot_describe_an_answer_the_owner_gave_themselves() {
        // "Confirmed from their own space, by telephone" is not a thing that can have happened.
        assertThatThrownBy(() -> ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.ATTENDING, null, false,
                ReplySource.OWNER_APP, ReplyMediumCode.of("TELEPHONE"), null, null, REPLIED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void an_answer_records_who_gave_it_when_it_is_known() {
        EntityId partyId = EntityId.newId();
        Convocation answered = sent().reply(ConvocationReply.record(ConvocationReplyId.newId(),
                AttendanceReply.ATTENDING, null, false, ReplySource.OWNER_APP, null, partyId, null, REPLIED_AT, null));

        assertThat(answered.getRepliedByPartyId()).isEqualTo(partyId);
        assertThat(answered.getReplySource()).isEqualTo(ReplySource.OWNER_APP);
    }

    @Test
    void an_answer_withdrawn_back_to_no_reply_clears_its_whole_provenance() {
        Convocation withdrawn = generated()
                .reply(ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.ATTENDING, null, false,
                        ReplySource.OWNER_LINK, null, EntityId.newId(), "via le lien", REPLIED_AT, null))
                .reply(answer(AttendanceReply.NO_REPLY, ReplySource.OWNER_LINK, REPLIED_AT.plusSeconds(60)));

        assertThat(withdrawn.getAttendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
        assertThat(withdrawn.getRepliedAt()).isNull();
        assertThat(withdrawn.getReplySource()).isNull();
        assertThat(withdrawn.getRepliedByPartyId()).isNull();
        assertThat(withdrawn.getReplyNote()).isNull();
    }

    @Test
    void a_withdrawal_is_itself_an_event_the_history_keeps() {
        // The columns drop the provenance of a withdrawal - no answer stands, so no source can
        // describe one. The history keeps who took it back, which is the fact worth having.
        Convocation withdrawn = generated()
                .reply(answer(AttendanceReply.ATTENDING, ReplySource.OWNER_LINK, REPLIED_AT))
                .reply(answer(AttendanceReply.NO_REPLY, ReplySource.OWNER_APP, REPLIED_AT.plusSeconds(60)));

        assertThat(withdrawn.getReplies()).hasSize(2);
        ConvocationReply last = withdrawn.getReplies().get(1);
        assertThat(last.isWithdrawal()).isTrue();
        assertThat(last.getSource()).isEqualTo(ReplySource.OWNER_APP);
    }

    @Test
    void an_answer_without_a_source_cannot_exist() {
        // Mirrors the CHECK constraint on the table: knowing someone confirmed without knowing
        // how it was obtained is the gap this whole field exists to close.
        assertThatThrownBy(() -> ConvocationReply.record(ConvocationReplyId.newId(), AttendanceReply.ATTENDING, null,
                false, null, null, null, null, REPLIED_AT, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Convocation.reconstruct(ConvocationId.newId(), GeneralMeetingId.newId(),
                EntityId.newId(), VotingWeight.perUnit(), List.of(), List.of(), "token-3", ShortCode.of("code02"),
                AttendanceReply.ATTENDING, REPLIED_AT, null, null, null, null, null, false, false, null, null, null,
                null))
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
                EntityId.newId(), VotingWeight.perUnit(), List.of(), List.of(), "token-4", ShortCode.of("code03"),
                AttendanceReply.NO_REPLY, null, null, null, null, null, null, false, true, null, null, null, null))
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
