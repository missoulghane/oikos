package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.command.CastVoteCommand;
import com.architek.oikos.meeting.application.command.CheckInConvocationCommand;
import com.architek.oikos.meeting.application.command.CloseGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.CloseVoteSessionCommand;
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.command.GenerateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.OpenVoteSessionCommand;
import com.architek.oikos.meeting.application.command.PublishMeetingMinutesCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.UpdateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.command.ValidateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.CastVoteUseCase;
import com.architek.oikos.meeting.application.port.in.CheckInConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.CloseGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.CloseVoteSessionUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.GetGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GetMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.OpenGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.OpenVoteSessionUseCase;
import com.architek.oikos.meeting.application.port.in.PublishMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.UpdateMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.ValidateMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.query.GetGeneralMeetingQuery;
import com.architek.oikos.meeting.application.query.GetMeetingMinutesQuery;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.domain.exception.MeetingNotClosedException;
import com.architek.oikos.meeting.domain.exception.MinutesLockedException;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The last leg of the cycle, on a real PostgreSQL: a closed session, minutes
 * drafted from its data, completed, validated, published.
 *
 * <p>What only this level can show is the freezing. Everywhere else in the
 * module the figures are recomputed on read; here they must stop moving. The
 * test therefore changes the underlying data after publication and asserts
 * that the published record does not follow - which is the entire reason
 * MeetingMinutes.content exists.
 */
@TestPropertySource(properties = "oikos.mail.enabled=false")
class MeetingMinutesPostgresIntegrationTest extends PostgresIntegrationTestBase {

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
    private CloseVoteSessionUseCase closeVoteSessionUseCase;

    @Autowired
    private CloseGeneralMeetingUseCase closeGeneralMeetingUseCase;

    @Autowired
    private GenerateMeetingMinutesUseCase generateMeetingMinutesUseCase;

    @Autowired
    private UpdateMeetingMinutesUseCase updateMeetingMinutesUseCase;

    @Autowired
    private ValidateMeetingMinutesUseCase validateMeetingMinutesUseCase;

    @Autowired
    private PublishMeetingMinutesUseCase publishMeetingMinutesUseCase;

    @Autowired
    private GetGeneralMeetingUseCase getGeneralMeetingUseCase;

    @Autowired
    private GetMeetingMinutesUseCase getMeetingMinutesUseCase;

    private EntityId propertyId;
    private EntityId presentUnitId;
    private EntityId absentUnitId;
    private EntityId syndicUserId;
    private GeneralMeetingId meetingId;
    private AgendaItemId itemId;

    @BeforeEach
    void runAWholeMeetingUpToItsClosing() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String unique = UUID.randomUUID().toString();
        propertyId = EntityId.of(UUID.randomUUID());
        UUID buildingId = UUID.randomUUID();
        UUID unitTypeId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        presentUnitId = EntityId.of(UUID.randomUUID());
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
        insertUnit(presentUnitId, buildingId, unitTypeId, "Appartement 1", new BigDecimal("700.00"), now);
        insertUnit(absentUnitId, buildingId, unitTypeId, "Appartement 2", new BigDecimal("300.00"), now);
        jdbcTemplate.update("insert into party (id, property_id, full_name, party_type, email, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Karim Benali', 'INDIVIDUAL', ?, ?, ?, 0)",
                partyId, propertyId.value(), "karim." + unique + "@example.com", now, now);
        jdbcTemplate.update("insert into unit_ownership (id, unit_id, party_id, property_id, ownership_share, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, 100.00, ?, ?, 0)",
                UUID.randomUUID(), presentUnitId.value(), partyId, propertyId.value(), now, now);
        jdbcTemplate.update("insert into app_user (id, email, full_name, password_hash, verified, enabled, "
                        + "created_date, last_modified_date, version) values (?, ?, 'Syndic', 'x', true, true, ?, ?, 0)",
                syndicUserId.value(), "syndic." + unique + "@example.com", now, now);

        meetingId = createGeneralMeetingUseCase.create(
                new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG ordinaire 2026", SESSION_DATE, VENUE));
        itemId = addAgendaItemUseCase.add(new AddAgendaItemCommand(meetingId, "Ravalement de façade", null,
                MajorityRule.ABSOLUTE)).id();
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(meetingId, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(
                new GenerateConvocationsCommand(meetingId));
        checkInConvocationUseCase.checkIn(new CheckInConvocationCommand(convocationOf(presentUnitId).id(),
                AttendanceMode.ON_SITE, null));
        openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(meetingId, false));
        openVoteSessionUseCase.open(new OpenVoteSessionCommand(itemId));
        castVoteUseCase.cast(new CastVoteCommand(itemId, presentUnitId, VoteChoice.FOR, syndicUserId));
        closeVoteSessionUseCase.close(new CloseVoteSessionCommand(itemId));
    }

    private void insertUnit(EntityId unitId, UUID buildingId, UUID unitTypeId, String unitNumber, BigDecimal shares,
                             OffsetDateTime now) {
        jdbcTemplate.update("insert into unit (id, building_id, property_id, unit_number, unit_type_id, shares, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                unitId.value(), buildingId, propertyId.value(), unitNumber, unitTypeId, shares, now, now);
    }

    private ConvocationView convocationOf(EntityId unitId) {
        return listConvocationsByMeetingUseCase.listConvocations(new ListConvocationsByMeetingQuery(meetingId, null))
                .stream().filter(view -> view.unitId().equals(unitId)).findFirst().orElseThrow();
    }

    private MeetingMinutesView draftMinutes() {
        closeGeneralMeetingUseCase.close(new CloseGeneralMeetingCommand(meetingId));
        return generateMeetingMinutesUseCase.generate(new GenerateMeetingMinutesCommand(meetingId));
    }

    @Test
    void the_minutes_of_a_session_still_under_way_cannot_be_drafted() {
        assertThatThrownBy(() -> generateMeetingMinutesUseCase.generate(new GenerateMeetingMinutesCommand(meetingId)))
                .isInstanceOf(MeetingNotClosedException.class);
    }

    @Test
    void the_draft_states_the_attendance_the_rule_and_the_outcome() {
        String content = draftMinutes().content();

        assertThat(content).contains("Résidence Al Amal");
        assertThat(content).contains("1 lot(s) présent(s) ou représenté(s) sur 2");
        assertThat(content).contains("700 voix sur 1000");
        assertThat(content).contains("Ravalement de façade");
        assertThat(content).contains("majorité absolue des voix de la copropriété");
        assertThat(content).contains("Résolution adoptée");
    }

    @Test
    void the_syndic_can_complete_the_draft_and_validating_freezes_it() {
        draftMinutes();
        updateMeetingMinutesUseCase.update(new UpdateMeetingMinutesCommand(meetingId,
                "<h1>PV</h1><p>Débat sur le choix du prestataire.</p>"));

        MeetingMinutesView validated = validateMeetingMinutesUseCase.validate(
                new ValidateMeetingMinutesCommand(meetingId));

        assertThat(validated.status()).isEqualTo(MinutesStatus.UNDER_REVIEW);
        assertThat(validated.content()).contains("Débat sur le choix du prestataire");
        assertThatThrownBy(() -> updateMeetingMinutesUseCase.update(
                new UpdateMeetingMinutesCommand(meetingId, "<p>trop tard</p>")))
                .isInstanceOf(MinutesLockedException.class);
    }

    @Test
    void publishing_files_the_pdf_and_ends_the_meetings_cycle() {
        draftMinutes();
        validateMeetingMinutesUseCase.validate(new ValidateMeetingMinutesCommand(meetingId));

        MeetingMinutesView published = publishMeetingMinutesUseCase.publish(
                new PublishMeetingMinutesCommand(meetingId, syndicUserId));

        assertThat(published.status()).isEqualTo(MinutesStatus.PUBLISHED);
        assertThat(published.publishedAt()).isNotNull();

        Long documents = jdbcTemplate.queryForObject(
                "select count(*) from document where owner_type = 'MEETING_MINUTES' and owner_id = ?", Long.class,
                meetingId.asUuid());
        assertThat(documents).isEqualTo(1L);

        GeneralMeetingView meeting = getGeneralMeetingUseCase.getGeneralMeeting(new GetGeneralMeetingQuery(meetingId));
        assertThat(meeting.status()).isEqualTo(MeetingStatus.MINUTES_PUBLISHED);
    }

    @Test
    void published_minutes_do_not_follow_the_data_they_were_made_from() {
        // The point of freezing: sell a lot, correct a tantième, and the record of a meeting
        // already held must read exactly as it did the day it was published.
        draftMinutes();
        validateMeetingMinutesUseCase.validate(new ValidateMeetingMinutesCommand(meetingId));
        String publishedContent = publishMeetingMinutesUseCase.publish(
                new PublishMeetingMinutesCommand(meetingId, syndicUserId)).content();

        jdbcTemplate.update("update unit set shares = 5000.00 where id = ?", absentUnitId.value());
        jdbcTemplate.update("update property set name = 'Résidence Renommée' where id = ?", propertyId.value());

        String reRead = getMeetingMinutesUseCase.getMinutes(new GetMeetingMinutesQuery(meetingId)).content();
        assertThat(reRead).isEqualTo(publishedContent);
        assertThat(reRead).contains("700 voix sur 1000");
        assertThat(reRead).contains("Résidence Al Amal");
        assertThat(reRead).doesNotContain("Résidence Renommée");
    }

    @Test
    void regenerating_a_draft_discards_the_manual_edits() {
        draftMinutes();
        updateMeetingMinutesUseCase.update(new UpdateMeetingMinutesCommand(meetingId, "<p>Notes manuelles</p>"));

        MeetingMinutesView regenerated = generateMeetingMinutesUseCase.generate(
                new GenerateMeetingMinutesCommand(meetingId));

        assertThat(regenerated.content()).doesNotContain("Notes manuelles");
        assertThat(regenerated.status()).isEqualTo(MinutesStatus.DRAFT);
    }

    @Test
    void a_meeting_opened_without_quorum_says_so_in_its_record() {
        jdbcTemplate.update("insert into meeting_quorum_setting (id, property_id, meeting_type, quorum_percentage, "
                        + "created_date, last_modified_date, version) values (?, ?, 'EXTRAORDINARY', 90.00, ?, ?, 0)",
                UUID.randomUUID(), propertyId.value(), OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC));
        GeneralMeetingId forced = createGeneralMeetingUseCase.create(
                new CreateGeneralMeetingCommand(propertyId, MeetingType.EXTRAORDINARY, "AG extraordinaire", SESSION_DATE, VENUE));
        addAgendaItemUseCase.add(new AddAgendaItemCommand(forced, "Point unique", null, MajorityRule.SIMPLE));
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(forced, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(
                new GenerateConvocationsCommand(forced));
        openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(forced, true));
        closeGeneralMeetingUseCase.close(new CloseGeneralMeetingCommand(forced));

        String content = generateMeetingMinutesUseCase.generate(new GenerateMeetingMinutesCommand(forced)).content();

        assertThat(content).contains("Quorum requis : 90 %");
        assertThat(content).contains("non atteint");
        assertThat(content).contains("La séance a été ouverte alors que le quorum n'était pas atteint");
    }

    @Test
    void the_agenda_appears_in_reading_order_as_numbered_resolutions() {
        GeneralMeetingId multi = createGeneralMeetingUseCase.create(
                new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG à plusieurs points", SESSION_DATE, VENUE));
        for (String label : List.of("Premier point", "Deuxième point", "Troisième point")) {
            addAgendaItemUseCase.add(new AddAgendaItemCommand(multi, label, null, MajorityRule.SIMPLE));
        }
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(multi, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(
                new GenerateConvocationsCommand(multi));
        openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(multi, true));
        closeGeneralMeetingUseCase.close(new CloseGeneralMeetingCommand(multi));

        String content = generateMeetingMinutesUseCase.generate(new GenerateMeetingMinutesCommand(multi)).content();

        assertThat(content).contains("Résolution n° 1 — Premier point");
        assertThat(content).contains("Résolution n° 2 — Deuxième point");
        assertThat(content).contains("Résolution n° 3 — Troisième point");
    }
}
