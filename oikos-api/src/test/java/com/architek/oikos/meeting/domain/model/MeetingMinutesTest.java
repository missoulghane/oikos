package com.architek.oikos.meeting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.architek.oikos.meeting.domain.exception.InvalidMinutesStatusTransitionException;
import com.architek.oikos.meeting.domain.exception.MinutesLockedException;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingMinutesId;
import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;

class MeetingMinutesTest {

    private static final Instant PUBLISHED_AT = Instant.parse("2026-09-20T10:00:00Z");

    private static MeetingMinutes draft() {
        return MeetingMinutes.draft(MeetingMinutesId.newId(), GeneralMeetingId.newId(),
                "<h1>Procès-verbal</h1><p>Résolution n° 1 adoptée.</p>");
    }

    @Test
    void a_generated_minute_starts_as_an_editable_draft() {
        MeetingMinutes minutes = draft();

        assertThat(minutes.getStatus()).isEqualTo(MinutesStatus.DRAFT);
        assertThat(minutes.getPublishedAt()).isNull();
    }

    @Test
    void the_draft_can_be_completed_before_validation() {
        MeetingMinutes completed = draft().withContent("<h1>PV</h1><p>Observations du syndic.</p>");

        assertThat(completed.getContent()).contains("Observations du syndic");
        assertThat(completed.getStatus()).isEqualTo(MinutesStatus.DRAFT);
    }

    @Test
    void validating_freezes_the_text() {
        MeetingMinutes validated = draft().validate();

        assertThat(validated.getStatus()).isEqualTo(MinutesStatus.UNDER_REVIEW);
        assertThatThrownBy(() -> validated.withContent("<p>Correction tardive</p>"))
                .isInstanceOf(MinutesLockedException.class);
    }

    @Test
    void validated_minutes_cannot_be_regenerated_from_the_session_data_either() {
        // Otherwise "regenerate" would be a way around the lock that "edit" refuses.
        MeetingMinutes validated = draft().validate();

        assertThatThrownBy(() -> validated.regeneratedWith("<p>Nouveau brouillon</p>"))
                .isInstanceOf(MinutesLockedException.class);
    }

    @Test
    void regenerating_a_draft_replaces_its_content_and_keeps_it_a_draft() {
        MeetingMinutes regenerated = draft().withContent("<p>Notes manuelles</p>")
                .regeneratedWith("<p>Contenu recalculé</p>");

        assertThat(regenerated.getContent()).isEqualTo("<p>Contenu recalculé</p>");
        assertThat(regenerated.getStatus()).isEqualTo(MinutesStatus.DRAFT);
    }

    @Test
    void publishing_records_when_it_happened() {
        MeetingMinutes published = draft().validate().publish(PUBLISHED_AT);

        assertThat(published.getStatus()).isEqualTo(MinutesStatus.PUBLISHED);
        assertThat(published.getPublishedAt()).isEqualTo(PUBLISHED_AT);
    }

    @Test
    void a_draft_cannot_be_published_without_being_validated_first() {
        assertThatThrownBy(() -> draft().publish(PUBLISHED_AT))
                .isInstanceOf(InvalidMinutesStatusTransitionException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void published_minutes_are_final() {
        // A record every copropriétaire has received cannot be edited, re-validated or re-published.
        MeetingMinutes published = draft().validate().publish(PUBLISHED_AT);

        assertThatThrownBy(() -> published.withContent("<p>x</p>")).isInstanceOf(MinutesLockedException.class);
        assertThatThrownBy(published::validate).isInstanceOf(InvalidMinutesStatusTransitionException.class);
        assertThatThrownBy(() -> published.publish(PUBLISHED_AT))
                .isInstanceOf(InvalidMinutesStatusTransitionException.class);
    }

    @Test
    void empty_minutes_are_refused() {
        assertThatThrownBy(() -> MeetingMinutes.draft(MeetingMinutesId.newId(), GeneralMeetingId.newId(), "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void published_minutes_reconstructed_without_a_date_are_refused() {
        assertThatThrownBy(() -> MeetingMinutes.reconstruct(MeetingMinutesId.newId(), GeneralMeetingId.newId(),
                "<p>x</p>", MinutesStatus.PUBLISHED, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
