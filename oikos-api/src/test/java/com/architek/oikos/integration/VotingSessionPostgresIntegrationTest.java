package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.command.CastVoteCommand;
import com.architek.oikos.meeting.application.command.CheckInConvocationCommand;
import com.architek.oikos.meeting.application.command.CloseVoteSessionCommand;
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.OpenVoteSessionCommand;
import com.architek.oikos.meeting.application.command.RecordShowOfHandsCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.CastVoteUseCase;
import com.architek.oikos.meeting.application.port.in.CheckInConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.CloseVoteSessionUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.GetAgendaItemResultUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ListVotesByAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.OpenGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.OpenVoteSessionUseCase;
import com.architek.oikos.meeting.application.port.in.RecordShowOfHandsUseCase;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.query.GetAgendaItemResultQuery;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.application.query.ListVotesByAgendaItemQuery;
import com.architek.oikos.meeting.domain.exception.UnitNotCheckedInException;
import com.architek.oikos.meeting.domain.exception.VoteSessionNotOpenException;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.meeting.domain.valueobject.VoteOutcome;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A whole session on a real PostgreSQL: three lots with different tantièmes,
 * two of them present, a ballot opened, votes cast, the ballot closed.
 *
 * <p>What only this level can show is that the weights actually used in the
 * tally are the ones snapshotted on the convocations at commit time - the
 * chain vote → convocation → weight crosses three tables and is what every
 * result depends on. It also pins the uniqueness of (agenda item, lot):
 * recasting must update the row, not add a second voice for the same lot.
 */
@TestPropertySource(properties = "oikos.mail.enabled=false")
class VotingSessionPostgresIntegrationTest extends PostgresIntegrationTestBase {

    private static final Instant SESSION_DATE = Instant.parse("2026-09-15T17:00:00Z");
    private static final MeetingVenue VENUE = MeetingVenue.onSite("12 rue des Orangers, Casablanca");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CreateGeneralMeetingUseCase createGeneralMeetingUseCase;

    @Autowired
    private AddAgendaItemUseCase addAgendaItemUseCase;

    @Autowired
    private ScheduleGeneralMeetingUseCase scheduleGeneralMeetingUseCase;

    @Autowired
    private GenerateConvocationsUseCase generateConvocationsUseCase;

    @Autowired
    private ListConvocationsByMeetingUseCase listConvocationsByMeetingUseCase;

    @Autowired
    private CheckInConvocationUseCase checkInConvocationUseCase;

    @Autowired
    private OpenGeneralMeetingUseCase openGeneralMeetingUseCase;

    @Autowired
    private OpenVoteSessionUseCase openVoteSessionUseCase;

    @Autowired
    private CastVoteUseCase castVoteUseCase;

    @Autowired
    private RecordShowOfHandsUseCase recordShowOfHandsUseCase;

    @Autowired
    private CloseVoteSessionUseCase closeVoteSessionUseCase;

    @Autowired
    private ListVotesByAgendaItemUseCase listVotesByAgendaItemUseCase;

    @Autowired
    private GetAgendaItemResultUseCase getAgendaItemResultUseCase;

    private EntityId propertyId;
    private EntityId bigUnitId;
    private EntityId smallUnitId;
    private EntityId absentUnitId;
    private EntityId syndicUserId;
    private GeneralMeetingId meetingId;
    private AgendaItemId absoluteItemId;
    private AgendaItemId simpleItemId;

    /**
     * 600 / 300 / 100 tantièmes so that the three denominators give visibly
     * different answers: the two lots present hold 900 of 1000 voices.
     */
    @BeforeEach
    void seedSessionInProgress() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String unique = UUID.randomUUID().toString();
        propertyId = EntityId.of(UUID.randomUUID());
        UUID buildingId = UUID.randomUUID();
        UUID unitTypeId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        bigUnitId = EntityId.of(UUID.randomUUID());
        smallUnitId = EntityId.of(UUID.randomUUID());
        absentUnitId = EntityId.of(UUID.randomUUID());
        syndicUserId = EntityId.of(UUID.randomUUID());

        jdbcTemplate.update("insert into property (id, name, address, created_date, last_modified_date, version, "
                        + "dues_calculation_mode) values (?, ?, ?, ?, ?, 0, 'SHARES')",
                propertyId.value(), "Résidence Al Amal", "12 rue des Orangers", now, now);
        jdbcTemplate.update("insert into building (id, property_id, name, floor_count, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Bâtiment A', 4, ?, ?, 0)",
                buildingId, propertyId.value(), now, now);
        jdbcTemplate.update("insert into unit_type_definition (id, property_id, name, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Appartement', ?, ?, 0)",
                unitTypeId, propertyId.value(), now, now);
        insertUnit(bigUnitId, buildingId, unitTypeId, "Appartement 1", new BigDecimal("600.00"), now);
        insertUnit(smallUnitId, buildingId, unitTypeId, "Appartement 2", new BigDecimal("300.00"), now);
        insertUnit(absentUnitId, buildingId, unitTypeId, "Appartement 3", new BigDecimal("100.00"), now);

        jdbcTemplate.update("insert into party (id, property_id, full_name, party_type, email, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Karim Benali', 'INDIVIDUAL', ?, ?, ?, 0)",
                partyId, propertyId.value(), "karim." + unique + "@example.com", now, now);
        for (EntityId unitId : List.of(bigUnitId, smallUnitId, absentUnitId)) {
            jdbcTemplate.update("insert into unit_ownership (id, unit_id, party_id, property_id, ownership_share, "
                            + "created_date, last_modified_date, version) values (?, ?, ?, ?, 100.00, ?, ?, 0)",
                    UUID.randomUUID(), unitId.value(), partyId, propertyId.value(), now, now);
        }
        jdbcTemplate.update("insert into app_user (id, email, full_name, password_hash, verified, enabled, "
                        + "created_date, last_modified_date, version) values (?, ?, 'Syndic', 'x', true, true, ?, ?, 0)",
                syndicUserId.value(), "syndic." + unique + "@example.com", now, now);

        meetingId = createGeneralMeetingUseCase.create(
                new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG ordinaire 2026", SESSION_DATE, VENUE));
        absoluteItemId = addAgendaItemUseCase.add(new AddAgendaItemCommand(meetingId, "Ravalement de façade", null,
                MajorityRule.ABSOLUTE)).id();
        simpleItemId = addAgendaItemUseCase.add(new AddAgendaItemCommand(meetingId, "Choix du prestataire", null,
                MajorityRule.SIMPLE)).id();
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(meetingId, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(
                new GenerateConvocationsCommand(meetingId));

        checkIn(bigUnitId);
        checkIn(smallUnitId);
        openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(meetingId, false));
    }

    private void insertUnit(EntityId unitId, UUID buildingId, UUID unitTypeId, String unitNumber, BigDecimal shares,
                             OffsetDateTime now) {
        jdbcTemplate.update("insert into unit (id, building_id, property_id, unit_number, unit_type_id, shares, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                unitId.value(), buildingId, propertyId.value(), unitNumber, unitTypeId, shares, now, now);
    }

    private void checkIn(EntityId unitId) {
        checkInConvocationUseCase.checkIn(new CheckInConvocationCommand(convocationOf(unitId).id(),
                AttendanceMode.ON_SITE, null));
    }

    private ConvocationView convocationOf(EntityId unitId) {
        return listConvocationsByMeetingUseCase.listConvocations(new ListConvocationsByMeetingQuery(meetingId, null))
                .stream().filter(view -> view.unitId().equals(unitId)).findFirst().orElseThrow();
    }

    private AgendaItemResultView resultOf(AgendaItemId itemId) {
        return getAgendaItemResultUseCase.getResult(new GetAgendaItemResultQuery(itemId));
    }

    @Test
    void the_tally_uses_the_weights_snapshotted_on_the_convocations() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(absoluteItemId));
        castVoteUseCase.cast(new CastVoteCommand(absoluteItemId, bigUnitId, VoteChoice.FOR, syndicUserId));
        castVoteUseCase.cast(new CastVoteCommand(absoluteItemId, smallUnitId, VoteChoice.AGAINST, syndicUserId));

        AgendaItemResultView result = resultOf(absoluteItemId);

        assertThat(result.forWeight()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(result.againstWeight()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(result.expressedWeight()).isEqualByComparingTo(new BigDecimal("900.00"));
        assertThat(result.presentWeight()).isEqualByComparingTo(new BigDecimal("900.00"));
        // The absent lot still weighs in the total - what an absolute majority is measured against.
        assertThat(result.totalWeight()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void an_absolute_majority_needs_more_than_half_of_the_whole_copropriete() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(absoluteItemId));
        castVoteUseCase.cast(new CastVoteCommand(absoluteItemId, bigUnitId, VoteChoice.FOR, syndicUserId));
        castVoteUseCase.cast(new CastVoteCommand(absoluteItemId, smallUnitId, VoteChoice.AGAINST, syndicUserId));

        assertThat(resultOf(absoluteItemId).outcome()).isEqualTo(VoteOutcome.ADOPTED);
    }

    @Test
    void the_same_votes_under_a_simple_majority_read_the_same_way_here() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(simpleItemId));
        castVoteUseCase.cast(new CastVoteCommand(simpleItemId, bigUnitId, VoteChoice.AGAINST, syndicUserId));
        castVoteUseCase.cast(new CastVoteCommand(simpleItemId, smallUnitId, VoteChoice.FOR, syndicUserId));

        assertThat(resultOf(simpleItemId).outcome()).isEqualTo(VoteOutcome.REJECTED);
    }

    @Test
    void recasting_replaces_the_lots_voice_instead_of_adding_a_second_one() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(simpleItemId));
        castVoteUseCase.cast(new CastVoteCommand(simpleItemId, bigUnitId, VoteChoice.FOR, syndicUserId));
        castVoteUseCase.cast(new CastVoteCommand(simpleItemId, bigUnitId, VoteChoice.AGAINST, syndicUserId));

        assertThat(listVotesByAgendaItemUseCase.listVotes(new ListVotesByAgendaItemQuery(simpleItemId))).hasSize(1);
        assertThat(resultOf(simpleItemId).againstCount()).isEqualTo(1);
        assertThat(resultOf(simpleItemId).forCount()).isZero();
    }

    @Test
    void a_show_of_hands_covers_the_room_and_only_the_room() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(absoluteItemId));

        AgendaItemResultView result = recordShowOfHandsUseCase.record(new RecordShowOfHandsCommand(absoluteItemId,
                VoteChoice.FOR, Map.of(smallUnitId, VoteChoice.ABSTENTION), syndicUserId));

        assertThat(result.forCount()).isEqualTo(1);
        assertThat(result.abstentionCount()).isEqualTo(1);
        assertThat(result.forWeight()).isEqualByComparingTo(new BigDecimal("600.00"));
        // The absent lot cast nothing: two votes recorded, not three.
        assertThat(listVotesByAgendaItemUseCase.listVotes(new ListVotesByAgendaItemQuery(absoluteItemId))).hasSize(2);
        assertThat(result.outcome()).isEqualTo(VoteOutcome.ADOPTED);
    }

    @Test
    void a_lot_that_is_not_in_the_room_cannot_vote() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(simpleItemId));

        assertThatThrownBy(() -> castVoteUseCase.cast(new CastVoteCommand(simpleItemId, absentUnitId, VoteChoice.FOR,
                syndicUserId))).isInstanceOf(UnitNotCheckedInException.class);
    }

    @Test
    void a_closed_ballot_accepts_nothing_more() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(simpleItemId));
        castVoteUseCase.cast(new CastVoteCommand(simpleItemId, bigUnitId, VoteChoice.FOR, syndicUserId));
        closeVoteSessionUseCase.close(new CloseVoteSessionCommand(simpleItemId));

        assertThatThrownBy(() -> castVoteUseCase.cast(new CastVoteCommand(simpleItemId, smallUnitId, VoteChoice.FOR,
                syndicUserId))).isInstanceOf(VoteSessionNotOpenException.class);
    }

    @Test
    void the_vote_sheet_names_each_lot_and_the_weight_it_carried() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(simpleItemId));
        castVoteUseCase.cast(new CastVoteCommand(simpleItemId, bigUnitId, VoteChoice.FOR, syndicUserId));

        assertThat(listVotesByAgendaItemUseCase.listVotes(new ListVotesByAgendaItemQuery(simpleItemId)))
                .singleElement().satisfies(vote -> {
                    assertThat(vote.unitNumber()).isEqualTo("Appartement 1");
                    assertThat(vote.buildingName()).isEqualTo("Bâtiment A");
                    assertThat(vote.votingWeight()).isEqualByComparingTo(new BigDecimal("600.00"));
                });
    }

    @Test
    void each_agenda_item_keeps_its_own_ballot() {
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(absoluteItemId));
        castVoteUseCase.cast(new CastVoteCommand(absoluteItemId, bigUnitId, VoteChoice.FOR, syndicUserId));

        AgendaItemResultView otherItem = resultOf(simpleItemId);
        assertThat(otherItem.forCount()).isZero();
        assertThat(otherItem.againstCount()).isZero();
        assertThat(otherItem.abstentionCount()).isZero();
    }
}
