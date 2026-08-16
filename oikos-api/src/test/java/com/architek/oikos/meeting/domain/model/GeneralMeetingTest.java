package com.architek.oikos.meeting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.architek.oikos.meeting.domain.exception.InvalidMeetingStatusTransitionException;
import com.architek.oikos.meeting.domain.exception.QuorumNotReachedException;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class GeneralMeetingTest {

    private static final Instant SESSION_DATE = Instant.parse("2026-09-15T17:00:00Z");
    private static final MeetingVenue VENUE = MeetingVenue.onSite("12 rue des Orangers, Casablanca");

    private static GeneralMeeting draft() {
        return GeneralMeeting.createDraft(GeneralMeetingId.newId(), EntityId.newId(), MeetingType.ORDINARY,
                "AG ordinaire 2026", null, null, QuorumPercentage.of(BigDecimal.valueOf(50)), VotingWeightMode.SHARES);
    }

    private static GeneralMeeting convened() {
        return draft().schedule(SESSION_DATE, VENUE).convene();
    }

    @Test
    void a_new_meeting_is_a_draft_with_neither_date_nor_venue() {
        GeneralMeeting meeting = draft();

        assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.DRAFT);
        assertThat(meeting.getScheduledAt()).isNull();
        assertThat(meeting.getVenue()).isNull();
        assertThat(meeting.isOpenedWithoutQuorum()).isFalse();
    }

    @Test
    void the_full_lifecycle_runs_draft_to_minutes_published() {
        GeneralMeeting published = draft()
                .schedule(SESSION_DATE, VENUE)
                .convene()
                .open(true, false)
                .close()
                .markMinutesPublished();

        assertThat(published.getStatus()).isEqualTo(MeetingStatus.MINUTES_PUBLISHED);
    }

    @Test
    void scheduling_carries_the_date_and_venue_it_was_given() {
        GeneralMeeting scheduled = draft().schedule(SESSION_DATE, VENUE);

        assertThat(scheduled.getStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(scheduled.getScheduledAt()).isEqualTo(SESSION_DATE);
        assertThat(scheduled.getVenue()).isEqualTo(VENUE);
    }

    @Test
    void a_meeting_cannot_skip_a_step_of_its_lifecycle() {
        GeneralMeeting draft = draft();

        assertThatThrownBy(draft::convene)
                .isInstanceOf(InvalidMeetingStatusTransitionException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("CONVENED");
    }

    @Test
    void a_meeting_never_goes_backwards() {
        GeneralMeeting scheduled = draft().schedule(SESSION_DATE, VENUE);

        assertThatThrownBy(() -> scheduled.schedule(SESSION_DATE, VENUE))
                .isInstanceOf(InvalidMeetingStatusTransitionException.class);
    }

    @Test
    void published_minutes_end_the_cycle() {
        GeneralMeeting published = draft().schedule(SESSION_DATE, VENUE).convene().open(true, false).close()
                .markMinutesPublished();

        for (MeetingStatus target : MeetingStatus.values()) {
            assertThat(published.getStatus().canTransitionTo(target)).isFalse();
        }
    }

    @Test
    void opening_without_quorum_is_refused_unless_it_is_forced() {
        GeneralMeeting convened = convened();

        assertThatThrownBy(() -> convened.open(false, false)).isInstanceOf(QuorumNotReachedException.class);
    }

    @Test
    void forcing_the_opening_records_that_the_quorum_was_missing() {
        GeneralMeeting opened = convened().open(false, true);

        assertThat(opened.getStatus()).isEqualTo(MeetingStatus.IN_PROGRESS);
        assertThat(opened.isOpenedWithoutQuorum()).isTrue();
    }

    @Test
    void forcing_is_ignored_when_the_quorum_is_actually_reached() {
        GeneralMeeting opened = convened().open(true, true);

        assertThat(opened.isOpenedWithoutQuorum()).isFalse();
    }

    @Test
    void a_draft_can_be_edited() {
        GeneralMeeting updated = draft().update(MeetingType.EXTRAORDINARY, "AG extraordinaire", SESSION_DATE, VENUE);

        assertThat(updated.getMeetingType()).isEqualTo(MeetingType.EXTRAORDINARY);
        assertThat(updated.getTitle()).isEqualTo("AG extraordinaire");
        assertThat(updated.getStatus()).isEqualTo(MeetingStatus.DRAFT);
    }

    @Test
    void the_date_and_venue_stay_editable_once_the_owners_have_been_convoked() {
        // Deliberate: no functional blocking rule for the time being (ADR 0002 §8). A syndic
        // correcting a typo in an address must not have to delete the assembly and start over.
        GeneralMeeting convened = convened();
        Instant postponed = SESSION_DATE.plusSeconds(86_400);

        GeneralMeeting corrected = convened.update(convened.getMeetingType(), "AG ordinaire 2026", postponed,
                MeetingVenue.onSite("14 rue des Orangers, Casablanca"));

        assertThat(corrected.getScheduledAt()).isEqualTo(postponed);
        assertThat(corrected.getVenue().address()).isEqualTo("14 rue des Orangers, Casablanca");
        // The status is untouched: editing is not a transition.
        assertThat(corrected.getStatus()).isEqualTo(MeetingStatus.CONVENED);
    }

    @Test
    void a_draft_may_have_its_pencilled_in_date_cleared() {
        GeneralMeeting withDate = draft().update(MeetingType.ORDINARY, "AG ordinaire 2026", SESSION_DATE, VENUE);

        GeneralMeeting cleared = withDate.update(MeetingType.ORDINARY, "AG ordinaire 2026", null, null);

        assertThat(cleared.getScheduledAt()).isNull();
        assertThat(cleared.getVenue()).isNull();
    }

    @Test
    void a_convoked_meeting_can_never_be_left_without_a_date_or_a_venue() {
        // Structural, not a business rule: the database says the same thing
        // (chk_general_meeting_scheduled), and it survives the relaxation above.
        GeneralMeeting convened = convened();

        assertThatThrownBy(() -> convened.update(MeetingType.ORDINARY, "AG", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void the_quorum_and_weighting_snapshots_survive_every_transition() {
        GeneralMeeting closed = draft().schedule(SESSION_DATE, VENUE).convene().open(true, false).close();

        assertThat(closed.getQuorumPercentage()).isEqualTo(QuorumPercentage.of(BigDecimal.valueOf(50)));
        assertThat(closed.getVotingWeightMode()).isEqualTo(VotingWeightMode.SHARES);
    }

    @Test
    void a_meeting_needs_a_title() {
        assertThatThrownBy(() -> GeneralMeeting.createDraft(GeneralMeetingId.newId(), EntityId.newId(),
                MeetingType.ORDINARY, "   ", null, null, QuorumPercentage.none(), VotingWeightMode.PER_UNIT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }

    @Test
    void a_reconstructed_meeting_past_draft_must_carry_its_date_and_venue() {
        assertThatThrownBy(() -> GeneralMeeting.reconstruct(GeneralMeetingId.newId(), EntityId.newId(),
                MeetingType.ORDINARY, MeetingStatus.CONVENED, "AG", null, null, QuorumPercentage.none(),
                VotingWeightMode.PER_UNIT, null, false, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatCode(() -> GeneralMeeting.reconstruct(GeneralMeetingId.newId(), EntityId.newId(),
                MeetingType.ORDINARY, MeetingStatus.DRAFT, "AG", null, null, QuorumPercentage.none(),
                VotingWeightMode.PER_UNIT, null, false, null))
                .doesNotThrowAnyException();
    }
}
