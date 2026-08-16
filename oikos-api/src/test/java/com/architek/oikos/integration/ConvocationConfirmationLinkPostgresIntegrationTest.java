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
import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.OpenGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.query.GetConvocationByTokenQuery;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.domain.exception.ConfirmationClosedException;
import com.architek.oikos.meeting.domain.exception.InvalidConvocationTokenException;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The confirmation link, on a real PostgreSQL - the path for copropriétaires
 * with no account, who until now could only answer by telephoning the syndic.
 *
 * <p>Tested at this level rather than with mocks because the two things that
 * can go wrong are both about real storage: the token has to be unique across
 * lots (a collision is an answer recorded on the wrong lot, and only a database
 * enforces that), and the anonymous lookup has to find its convocation by token
 * alone.
 */
@TestPropertySource(properties = "oikos.mail.enabled=false")
class ConvocationConfirmationLinkPostgresIntegrationTest extends PostgresIntegrationTestBase {

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
    private OpenGeneralMeetingUseCase openGeneralMeetingUseCase;

    @Autowired
    private GetConvocationByTokenUseCase getConvocationByTokenUseCase;

    @Autowired
    private ConfirmConvocationByTokenUseCase confirmConvocationByTokenUseCase;

    private EntityId propertyId;
    private UUID firstUnitId;
    private UUID secondUnitId;
    private GeneralMeetingId meetingId;

    @BeforeEach
    void seedPropertyAndConvokedMeeting() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        propertyId = EntityId.of(UUID.randomUUID());
        UUID buildingId = UUID.randomUUID();
        UUID unitTypeId = UUID.randomUUID();
        firstUnitId = UUID.randomUUID();
        secondUnitId = UUID.randomUUID();

        jdbcTemplate.update("insert into property (id, name, address, created_date, last_modified_date, version, "
                        + "dues_calculation_mode) values (?, ?, ?, ?, ?, 0, 'SHARES')",
                propertyId.value(), "Résidence Al Amal", "12 rue des Orangers", now, now);
        jdbcTemplate.update("insert into building (id, property_id, name, floor_count, created_date, "
                        + "last_modified_date, version) values (?, ?, ?, 4, ?, ?, 0)",
                buildingId, propertyId.value(), "Bâtiment A", now, now);
        jdbcTemplate.update("insert into unit_type_definition (id, property_id, name, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Appartement', ?, ?, 0)",
                unitTypeId, propertyId.value(), now, now);
        insertUnit(firstUnitId, buildingId, unitTypeId, "Appartement 1", new BigDecimal("120.00"), now);
        insertUnit(secondUnitId, buildingId, unitTypeId, "Appartement 2", new BigDecimal("80.00"), now);

        meetingId = createGeneralMeetingUseCase.create(new CreateGeneralMeetingCommand(propertyId,
                MeetingType.ORDINARY, "AG ordinaire 2026", SESSION_DATE, VENUE));
        addAgendaItemUseCase.add(new AddAgendaItemCommand(meetingId, "Approbation des comptes 2025", null,
                MajorityRule.SIMPLE));
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(meetingId, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(new GenerateConvocationsCommand(meetingId));
    }

    private void insertUnit(UUID unitId, UUID buildingId, UUID unitTypeId, String unitNumber, BigDecimal shares,
                             OffsetDateTime now) {
        jdbcTemplate.update("insert into unit (id, building_id, property_id, unit_number, unit_type_id, shares, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                unitId, buildingId, propertyId.value(), unitNumber, unitTypeId, shares, now, now);
    }

    /**
     * Read straight from the column: the token is deliberately absent from every
     * read model, so there is no view to get it from - which is itself the point
     * of the assertion below on the back-office payload.
     */
    private String tokenOf(UUID unitId) {
        return jdbcTemplate.queryForObject(
                "select confirmation_token from convocation where general_meeting_id = ? and unit_id = ?", String.class,
                meetingId.asUuid(), unitId);
    }

    private ConvocationView convocationOf(UUID unitId) {
        return listConvocationsByMeetingUseCase
                .listConvocations(new ListConvocationsByMeetingQuery(meetingId, null)).stream()
                .filter(view -> view.unitId().value().equals(unitId)).findFirst().orElseThrow();
    }

    @Test
    void generating_gives_every_lot_its_own_token() {
        // One per lot and never shared: the token is the only thing an anonymous visitor
        // presents, so a collision is an answer recorded on someone else's lot.
        String first = tokenOf(firstUnitId);
        String second = tokenOf(secondUnitId);

        assertThat(first).isNotBlank().isNotEqualTo(second);
        // 32 bytes, Base64-url without padding.
        assertThat(first).hasSize(43);
    }

    @Test
    void the_token_is_minted_at_generation_not_at_send_time() {
        // The link is printed on the letter a syndic posts, so it has to exist before
        // anything is sent - nothing was sent in this test at all.
        assertThat(convocationOf(firstUnitId).deliveries()).isEmpty();
        assertThat(tokenOf(firstUnitId)).isNotBlank();
    }

    @Test
    void the_link_shows_the_meeting_and_the_lot_it_concerns() {
        ConvocationConfirmationView view = getConvocationByTokenUseCase
                .getByToken(new GetConvocationByTokenQuery(tokenOf(firstUnitId)));

        assertThat(view.propertyName()).isEqualTo("Résidence Al Amal");
        assertThat(view.meetingTitle()).isEqualTo("AG ordinaire 2026");
        assertThat(view.unitNumber()).isEqualTo("Appartement 1");
        assertThat(view.buildingName()).isEqualTo("Bâtiment A");
        assertThat(view.attendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
        assertThat(view.stillOpen()).isTrue();
    }

    @Test
    void an_answer_through_the_link_is_recorded_as_coming_from_the_link() {
        // The whole point: this source cannot be reached from the authenticated endpoint,
        // and cannot be claimed by a client either - being here is what proves it.
        confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(tokenOf(firstUnitId), AttendanceReply.ATTENDING));

        ConvocationView convocation = convocationOf(firstUnitId);
        assertThat(convocation.attendanceReply()).isEqualTo(AttendanceReply.ATTENDING);
        assertThat(convocation.replySource()).isEqualTo(ReplySource.OWNER_LINK);
        assertThat(convocation.status()).isEqualTo(ConvocationStatus.CONFIRMED);
    }

    @Test
    void the_link_never_names_who_answered() {
        // The token proves the convocation was received, not which of several indivisaires
        // is clicking. Writing a party id here would be an invention.
        confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(tokenOf(firstUnitId), AttendanceReply.ATTENDING));

        assertThat(jdbcTemplate.queryForObject("select replied_by_party_id from convocation where unit_id = ?",
                UUID.class, firstUnitId)).isNull();
    }

    @Test
    void one_lots_link_answers_for_that_lot_only() {
        confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(tokenOf(firstUnitId), AttendanceReply.NOT_ATTENDING));

        assertThat(convocationOf(firstUnitId).attendanceReply()).isEqualTo(AttendanceReply.NOT_ATTENDING);
        assertThat(convocationOf(secondUnitId).attendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
    }

    @Test
    void changing_ones_mind_through_the_link_is_allowed() {
        // Same rule as everywhere else: the last answer before the session is the one that counts.
        String token = tokenOf(firstUnitId);
        confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, AttendanceReply.ATTENDING));

        ConvocationConfirmationView changed = confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, AttendanceReply.NOT_ATTENDING));

        assertThat(changed.attendanceReply()).isEqualTo(AttendanceReply.NOT_ATTENDING);
    }

    @Test
    void an_unknown_token_says_nothing_more_than_that_it_is_invalid() {
        // The endpoint is anonymous, so its error text is readable by anyone trying tokens.
        assertThatThrownBy(() -> getConvocationByTokenUseCase
                .getByToken(new GetConvocationByTokenQuery("clearly-not-a-real-token")))
                .isInstanceOf(InvalidConvocationTokenException.class)
                .hasMessageNotContainingAny("Appartement", "Al Amal", "AG ordinaire");

        assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand("clearly-not-a-real-token", AttendanceReply.ATTENDING)))
                .isInstanceOf(InvalidConvocationTokenException.class);
    }

    @Test
    void a_blank_token_is_refused_rather_than_matching_anything() {
        assertThatThrownBy(() -> getConvocationByTokenUseCase.getByToken(new GetConvocationByTokenQuery("")))
                .isInstanceOf(InvalidConvocationTokenException.class);
        assertThatThrownBy(() -> getConvocationByTokenUseCase.getByToken(new GetConvocationByTokenQuery(null)))
                .isInstanceOf(InvalidConvocationTokenException.class);
    }

    @Test
    void the_link_stops_accepting_an_answer_once_the_session_opens() {
        // Past the opening, presence is the check-in and nothing else (ADR 0002 §5).
        // Accepting here would let a lot appear to have "confirmed" a meeting nobody attended,
        // on the record the minutes are drawn from.
        String token = tokenOf(firstUnitId);
        openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(meetingId, true));

        assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, AttendanceReply.ATTENDING)))
                .isInstanceOf(ConfirmationClosedException.class);
    }

    @Test
    void the_page_still_opens_after_the_session_started_and_says_it_is_closed() {
        // A visitor following an old link deserves an explanation, not a 404.
        String token = tokenOf(firstUnitId);
        openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(meetingId, true));

        ConvocationConfirmationView view = getConvocationByTokenUseCase
                .getByToken(new GetConvocationByTokenQuery(token));

        assertThat(view.stillOpen()).isFalse();
        assertThat(view.meetingTitle()).isEqualTo("AG ordinaire 2026");
    }

    @Test
    void the_back_office_payload_never_carries_the_token() {
        // It travels in the email and on the printed letter. A tracking table returning one
        // token per lot would hand every lot's answer to whoever can read that page.
        List<ConvocationView> convocations = listConvocationsByMeetingUseCase
                .listConvocations(new ListConvocationsByMeetingQuery(meetingId, null));

        assertThat(convocations).isNotEmpty();
        assertThat(convocations.toString()).doesNotContain(tokenOf(firstUnitId));
    }
}
