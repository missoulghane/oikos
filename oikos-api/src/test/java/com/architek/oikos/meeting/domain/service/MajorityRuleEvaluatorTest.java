package com.architek.oikos.meeting.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.meeting.domain.valueobject.VoteId;
import com.architek.oikos.meeting.domain.valueobject.VoteOutcome;
import com.architek.oikos.meeting.domain.valueobject.VoteTally;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The truth table of the three majority rules. This is the part of the module
 * where a wrong line silently adopts a resolution nobody carried, so each rule
 * is exercised on both sides of its own boundary and against the denominators
 * it must NOT use.
 */
class MajorityRuleEvaluatorTest {

    private static final GeneralMeetingId MEETING = GeneralMeetingId.newId();
    private static final AgendaItemId ITEM = AgendaItemId.newId();
    private static final Instant AT = Instant.parse("2026-09-15T18:00:00Z");

    /** Builds a room: each entry is a lot's weight, whether it is present, and what it voted (null = did not vote). */
    private static final class Room {

        private final List<Convocation> convocations = new ArrayList<>();
        private final List<Vote> votes = new ArrayList<>();

        Room lot(int weight, boolean present, VoteChoice choice) {
            EntityId unitId = EntityId.newId();
            Convocation convocation = Convocation.generate(ConvocationId.newId(), MEETING, unitId,
                    VotingWeight.of(BigDecimal.valueOf(weight)), "token-1", ShortCode.of("code01"));
            convocations.add(present ? convocation.checkIn(AttendanceMode.ON_SITE, null, AT) : convocation);
            if (choice != null) {
                votes.add(Vote.cast(VoteId.newId(), ITEM, unitId, choice, AT, null));
            }
            return this;
        }

        VoteTally tally() {
            return VoteTally.of(votes, convocations);
        }
    }

    private static VoteOutcome outcome(MajorityRule rule, Room room) {
        return MajorityRuleEvaluator.evaluate(rule, room.tally());
    }

    // --- SIMPLE: for > against, abstentions ignored -------------------------------------------

    @Test
    void simple_majority_passes_on_one_more_voice_for_than_against() {
        assertThat(outcome(MajorityRule.SIMPLE, new Room()
                .lot(51, true, VoteChoice.FOR)
                .lot(50, true, VoteChoice.AGAINST)))
                .isEqualTo(VoteOutcome.ADOPTED);
    }

    @Test
    void simple_majority_fails_on_a_tie() {
        assertThat(outcome(MajorityRule.SIMPLE, new Room()
                .lot(50, true, VoteChoice.FOR)
                .lot(50, true, VoteChoice.AGAINST)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    @Test
    void simple_majority_ignores_abstentions_entirely() {
        // 10 for, 5 against, 900 abstaining: adopted. Counting abstentions against would flip it.
        assertThat(outcome(MajorityRule.SIMPLE, new Room()
                .lot(10, true, VoteChoice.FOR)
                .lot(5, true, VoteChoice.AGAINST)
                .lot(900, true, VoteChoice.ABSTENTION)))
                .isEqualTo(VoteOutcome.ADOPTED);
    }

    @Test
    void simple_majority_ignores_the_lots_that_stayed_home() {
        assertThat(outcome(MajorityRule.SIMPLE, new Room()
                .lot(10, true, VoteChoice.FOR)
                .lot(990, false, null)))
                .isEqualTo(VoteOutcome.ADOPTED);
    }

    // --- ABSOLUTE: for > half of the whole copropriété ----------------------------------------

    @Test
    void absolute_majority_is_measured_against_the_whole_copropriete_not_the_room() {
        // Unanimous among the lots present, but they hold 400 of 1000 voices: not carried.
        assertThat(outcome(MajorityRule.ABSOLUTE, new Room()
                .lot(400, true, VoteChoice.FOR)
                .lot(600, false, null)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    @Test
    void absolute_majority_passes_just_above_half_of_all_the_voices() {
        assertThat(outcome(MajorityRule.ABSOLUTE, new Room()
                .lot(501, true, VoteChoice.FOR)
                .lot(499, false, null)))
                .isEqualTo(VoteOutcome.ADOPTED);
    }

    @Test
    void exactly_half_the_voices_is_not_an_absolute_majority() {
        assertThat(outcome(MajorityRule.ABSOLUTE, new Room()
                .lot(500, true, VoteChoice.FOR)
                .lot(500, true, VoteChoice.AGAINST)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    @Test
    void an_odd_total_splits_without_rounding_a_result_into_existence() {
        // 2 for out of 3 voices: above 1.5, adopted. Rounding the half up to 2 would reject it.
        assertThat(outcome(MajorityRule.ABSOLUTE, new Room()
                .lot(1, true, VoteChoice.FOR)
                .lot(1, true, VoteChoice.FOR)
                .lot(1, true, VoteChoice.AGAINST)))
                .isEqualTo(VoteOutcome.ADOPTED);
    }

    @Test
    void abstentions_do_not_help_an_absolute_majority() {
        assertThat(outcome(MajorityRule.ABSOLUTE, new Room()
                .lot(400, true, VoteChoice.FOR)
                .lot(300, true, VoteChoice.ABSTENTION)
                .lot(300, true, VoteChoice.AGAINST)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    // --- UNANIMITY: no opposition, no abstention, at least one voice for -----------------------

    @Test
    void unanimity_needs_every_present_lot_behind_it() {
        assertThat(outcome(MajorityRule.UNANIMITY, new Room()
                .lot(100, true, VoteChoice.FOR)
                .lot(200, true, VoteChoice.FOR)
                .lot(700, false, null)))
                .isEqualTo(VoteOutcome.ADOPTED);
    }

    @Test
    void a_single_abstention_breaks_unanimity() {
        assertThat(outcome(MajorityRule.UNANIMITY, new Room()
                .lot(900, true, VoteChoice.FOR)
                .lot(1, true, VoteChoice.ABSTENTION)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    @Test
    void a_single_opposition_breaks_unanimity() {
        assertThat(outcome(MajorityRule.UNANIMITY, new Room()
                .lot(900, true, VoteChoice.FOR)
                .lot(1, true, VoteChoice.AGAINST)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    @Test
    void an_item_nobody_voted_on_is_not_adopted_unanimously() {
        // Without the "at least one voice for" condition, an untouched ballot would come out
        // ADOPTED - no against, no abstention, and a room full of people.
        assertThat(outcome(MajorityRule.UNANIMITY, new Room()
                .lot(500, true, null)
                .lot(500, true, null)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    @Test
    void unanimity_in_an_empty_room_is_not_unanimity() {
        assertThat(outcome(MajorityRule.UNANIMITY, new Room().lot(1000, false, null)))
                .isEqualTo(VoteOutcome.REJECTED);
    }

    // --- one lot one voice: the same rules on flat weights ------------------------------------

    @Test
    void the_rules_read_identically_when_every_lot_weighs_one() {
        Room room = new Room()
                .lot(1, true, VoteChoice.FOR)
                .lot(1, true, VoteChoice.FOR)
                .lot(1, true, VoteChoice.AGAINST)
                .lot(1, false, null);

        assertThat(outcome(MajorityRule.SIMPLE, room)).isEqualTo(VoteOutcome.ADOPTED);
        // 2 for out of 4 lots is not more than half.
        assertThat(MajorityRuleEvaluator.evaluate(MajorityRule.ABSOLUTE, room.tally())).isEqualTo(VoteOutcome.REJECTED);
        assertThat(MajorityRuleEvaluator.evaluate(MajorityRule.UNANIMITY, room.tally())).isEqualTo(VoteOutcome.REJECTED);
    }
}
