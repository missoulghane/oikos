package com.architek.oikos.meeting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.meeting.domain.exception.InvalidVoteSessionTransitionException;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;

class AgendaItemTest {

    private static AgendaItem item() {
        return AgendaItem.create(AgendaItemId.newId(), GeneralMeetingId.newId(), "Approbation des comptes 2025",
                "Comptes arrêtés au 31/12/2025", 0, MajorityRule.ABSOLUTE);
    }

    @Test
    void a_new_item_has_no_ballot_open_and_accepts_no_vote() {
        AgendaItem created = item();

        assertThat(created.getVoteSessionStatus()).isEqualTo(VoteSessionStatus.NOT_OPENED);
        assertThat(created.acceptsVotes()).isFalse();
    }

    @Test
    void the_ballot_opens_then_closes() {
        AgendaItem open = item().openVoteSession();
        assertThat(open.acceptsVotes()).isTrue();

        AgendaItem closed = open.closeVoteSession();
        assertThat(closed.getVoteSessionStatus()).isEqualTo(VoteSessionStatus.CLOSED);
        assertThat(closed.acceptsVotes()).isFalse();
    }

    @Test
    void a_closed_ballot_never_reopens() {
        AgendaItem closed = item().openVoteSession().closeVoteSession();

        assertThatThrownBy(closed::openVoteSession)
                .isInstanceOf(InvalidVoteSessionTransitionException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void a_ballot_that_was_never_opened_cannot_be_closed() {
        assertThatThrownBy(() -> item().closeVoteSession()).isInstanceOf(InvalidVoteSessionTransitionException.class);
    }

    @Test
    void opening_twice_is_refused_rather_than_silently_accepted() {
        AgendaItem open = item().openVoteSession();

        assertThatThrownBy(open::openVoteSession).isInstanceOf(InvalidVoteSessionTransitionException.class);
    }

    @Test
    void moving_an_item_changes_only_its_position() {
        AgendaItem moved = item().moveTo(3);

        assertThat(moved.getPosition()).isEqualTo(3);
        assertThat(moved.getLabel()).isEqualTo("Approbation des comptes 2025");
        assertThat(moved.getMajorityRule()).isEqualTo(MajorityRule.ABSOLUTE);
    }

    @Test
    void a_position_is_never_negative() {
        assertThatThrownBy(() -> item().moveTo(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updating_may_change_the_majority_rule_of_a_point() {
        AgendaItem updated = item().update("Travaux de ravalement", null, MajorityRule.UNANIMITY);

        assertThat(updated.getMajorityRule()).isEqualTo(MajorityRule.UNANIMITY);
        assertThat(updated.getDescription()).isNull();
    }

    @Test
    void an_item_needs_a_label() {
        assertThatThrownBy(() -> item().update("  ", null, MajorityRule.SIMPLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("label");
    }
}
