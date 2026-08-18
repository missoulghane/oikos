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
import com.architek.oikos.meeting.application.command.ConfirmConvocationByCodeCommand;
import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByCodeUseCase;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.OpenGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.query.GetConvocationByCodeQuery;
import com.architek.oikos.meeting.application.query.GetConvocationByTokenQuery;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.domain.exception.ConfirmationClosedException;
import com.architek.oikos.meeting.domain.exception.InvalidConfirmationCodeException;
import com.architek.oikos.meeting.domain.exception.InvalidConvocationTokenException;
import com.architek.oikos.meeting.domain.exception.TooManyConfirmationAttemptsException;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
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
    private GetConvocationUseCase getConvocationUseCase;

    @Autowired
    private ConfirmConvocationByTokenUseCase confirmConvocationByTokenUseCase;

    @Autowired
    private ConfirmConvocationByCodeUseCase confirmConvocationByCodeUseCase;

    @Autowired
    private com.architek.oikos.meeting.application.port.in.GetConvocationByCodeUseCase getConvocationByCodeUseCase;

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

    private ShortCode meetingReference() {
        return ShortCode.of(jdbcTemplate.queryForObject("select public_reference from general_meeting where id = ?",
                String.class, meetingId.asUuid()));
    }

    private ShortCode codeOf(UUID unitId) {
        return ShortCode.of(jdbcTemplate.queryForObject(
                "select confirmation_code from convocation where general_meeting_id = ? and unit_id = ?", String.class,
                meetingId.asUuid(), unitId));
    }

    @Test
    void every_lot_gets_its_own_six_character_code_and_the_meeting_its_reference() {
        ShortCode first = codeOf(firstUnitId);
        ShortCode second = codeOf(secondUnitId);

        assertThat(first.value()).hasSize(6).isNotEqualTo(second.value());
        assertThat(meetingReference().value()).hasSize(6);
        // Digits and lowercase letters, minus the two that are misread on paper.
        assertThat(first.value()).matches("[0-9a-km-np-z]{6}");
        assertThat(meetingReference().value()).matches("[0-9a-f]{6}|[0-9a-km-np-z]{6}");
    }

    @Test
    void the_pair_of_codes_confirms_exactly_like_the_link() {
        ConvocationConfirmationView answered = confirmConvocationByCodeUseCase.confirm(
                new ConfirmConvocationByCodeCommand(meetingReference(), codeOf(firstUnitId),
                        AttendanceReply.ATTENDING, "203.0.113.7"));

        assertThat(answered.attendanceReply()).isEqualTo(AttendanceReply.ATTENDING);
        assertThat(convocationOf(firstUnitId).replySource()).isEqualTo(ReplySource.OWNER_LINK);
        assertThat(convocationOf(secondUnitId).attendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
    }

    @Test
    void a_code_belonging_to_another_meeting_does_not_open_this_one() {
        // Codes are unique per meeting, not globally - which is only safe because the
        // reference is always presented with them.
        GeneralMeetingId otherMeeting = createGeneralMeetingUseCase.create(new CreateGeneralMeetingCommand(propertyId,
                MeetingType.ORDINARY, "Autre AG", SESSION_DATE, VENUE));
        addAgendaItemUseCase.add(new AddAgendaItemCommand(otherMeeting, "Point", null, MajorityRule.SIMPLE));
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(otherMeeting, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(new GenerateConvocationsCommand(otherMeeting));
        ShortCode otherReference = ShortCode.of(jdbcTemplate.queryForObject(
                "select public_reference from general_meeting where id = ?", String.class, otherMeeting.asUuid()));

        assertThatThrownBy(() -> getConvocationByCodeUseCase.getByCode(
                new GetConvocationByCodeQuery(otherReference, codeOf(firstUnitId), "203.0.113.8")))
                .isInstanceOf(InvalidConvocationTokenException.class);
    }

    @Test
    void a_wrong_code_says_nothing_and_a_wrong_reference_says_the_same() {
        // Telling them apart would let someone confirm a reference for free, then spend every
        // remaining attempt on the code alone.
        assertThatThrownBy(() -> getConvocationByCodeUseCase.getByCode(
                new GetConvocationByCodeQuery(meetingReference(), ShortCode.of("zzzzzz"), "203.0.113.9")))
                .isInstanceOf(InvalidConvocationTokenException.class)
                .hasMessageNotContainingAny("Appartement", "Al Amal");

        assertThatThrownBy(() -> getConvocationByCodeUseCase.getByCode(
                new GetConvocationByCodeQuery(ShortCode.of("zzzzzz"), codeOf(firstUnitId), "203.0.113.10")))
                .isInstanceOf(InvalidConvocationTokenException.class)
                .hasMessageNotContainingAny("Appartement", "Al Amal");
    }

    @Test
    void walking_the_code_space_is_cut_off_after_a_few_attempts() {
        // The other half of the decision to offer a six-character code at all: 34^6 is only
        // out of reach if it cannot be tried in a loop.
        String attacker = "198.51.100.4";
        for (int attempt = 0; attempt < 10; attempt++) {
            assertThatThrownBy(() -> getConvocationByCodeUseCase.getByCode(
                    new GetConvocationByCodeQuery(meetingReference(), ShortCode.of("zzzzzz"), attacker)))
                    .isInstanceOf(InvalidConvocationTokenException.class);
        }

        assertThatThrownBy(() -> getConvocationByCodeUseCase.getByCode(
                new GetConvocationByCodeQuery(meetingReference(), ShortCode.of("zzzzzz"), attacker)))
                .isInstanceOf(TooManyConfirmationAttemptsException.class);

        // And the cap is per caller: one attacker must not lock out a whole copropriété.
        assertThat(getConvocationByCodeUseCase.getByCode(new GetConvocationByCodeQuery(meetingReference(),
                codeOf(firstUnitId), "203.0.113.11")).unitNumber()).isEqualTo("Appartement 1");
    }

    @Test
    void the_tracking_list_never_carries_a_code_and_the_detail_does() {
        // A hundred codes in one payload is the whole copropriété's answers handed to whoever
        // can open that screen.
        List<ConvocationView> list = listConvocationsByMeetingUseCase
                .listConvocations(new ListConvocationsByMeetingQuery(meetingId, null));

        assertThat(list).isNotEmpty()
                .allSatisfy(view -> assertThat(view.confirmationCode()).isNull());
        assertThat(list.toString()).doesNotContain(codeOf(firstUnitId).value());

        ConvocationView detail = getConvocationUseCase.getConvocation(
                new GetConvocationQuery(ConvocationId.of(convocationOf(firstUnitId).id().toString())));
        assertThat(detail.confirmationCode()).isEqualTo(codeOf(firstUnitId).value());
        assertThat(detail.meetingPublicReference()).isEqualTo(meetingReference().value());
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
        confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(tokenOf(firstUnitId),
                codeOf(firstUnitId), AttendanceReply.ATTENDING, "203.0.113.20"));

        ConvocationView convocation = convocationOf(firstUnitId);
        assertThat(convocation.attendanceReply()).isEqualTo(AttendanceReply.ATTENDING);
        assertThat(convocation.replySource()).isEqualTo(ReplySource.OWNER_LINK);
        assertThat(convocation.status()).isEqualTo(ConvocationStatus.CONFIRMED);
    }

    @Test
    void the_link_alone_does_not_answer_for_a_lot() {
        // A link is forwarded, printed, left on a table. Reading the page takes the link;
        // answering takes the code printed on the letter too (ADR 0002 §16).
        assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(
                tokenOf(firstUnitId), null, AttendanceReply.ATTENDING, "203.0.113.30")))
                .isInstanceOf(InvalidConfirmationCodeException.class);

        assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(
                tokenOf(firstUnitId), ShortCode.of("zzzzzz"), AttendanceReply.ATTENDING, "203.0.113.31")))
                .isInstanceOf(InvalidConfirmationCodeException.class);

        assertThat(convocationOf(firstUnitId).attendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
    }

    @Test
    void another_lots_code_does_not_answer_for_this_one() {
        // Both codes are valid, and both belong to this assembly - which is exactly the
        // mix-up a copropriétaire holding two convocations can make, and the one that would
        // record an answer on a neighbour's lot.
        assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(
                tokenOf(firstUnitId), codeOf(secondUnitId), AttendanceReply.ATTENDING, "203.0.113.32")))
                .isInstanceOf(InvalidConfirmationCodeException.class);

        assertThat(convocationOf(firstUnitId).attendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
        assertThat(convocationOf(secondUnitId).attendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
    }

    @Test
    void guessing_the_code_behind_a_leaked_link_is_capped_like_the_paper_path() {
        // Otherwise the check the link now carries would be six characters to walk, and the
        // leaked link is the very thing it exists to survive.
        String attacker = "198.51.100.9";
        String token = tokenOf(firstUnitId);
        for (int attempt = 0; attempt < 10; attempt++) {
            assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(
                    token, ShortCode.of("zzzzzz"), AttendanceReply.ATTENDING, attacker)))
                    .isInstanceOf(InvalidConfirmationCodeException.class);
        }

        assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(
                token, ShortCode.of("zzzzzz"), AttendanceReply.ATTENDING, attacker)))
                .isInstanceOf(TooManyConfirmationAttemptsException.class);

        // And the cap is per caller, here as there: one attacker must not lock out a whole
        // copropriété on the eve of its assembly.
        assertThat(confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(token,
                codeOf(firstUnitId), AttendanceReply.ATTENDING, "203.0.113.33")).attendanceReply())
                .isEqualTo(AttendanceReply.ATTENDING);
    }

    @Test
    void the_page_still_opens_on_the_link_alone() {
        // The code guards the answer, not the reading: someone who scanned the QR code must
        // see which assembly and which lot they are about to answer for before typing it.
        ConvocationConfirmationView view = getConvocationByTokenUseCase
                .getByToken(new GetConvocationByTokenQuery(tokenOf(firstUnitId)));

        assertThat(view.unitNumber()).isEqualTo("Appartement 1");
    }

    @Test
    void the_link_never_names_who_answered() {
        // The token proves the convocation was received, not which of several indivisaires
        // is clicking. Writing a party id here would be an invention.
        confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(tokenOf(firstUnitId),
                codeOf(firstUnitId), AttendanceReply.ATTENDING, "203.0.113.21"));

        assertThat(jdbcTemplate.queryForObject("select replied_by_party_id from convocation where unit_id = ?",
                UUID.class, firstUnitId)).isNull();
    }

    @Test
    void one_lots_link_answers_for_that_lot_only() {
        confirmConvocationByTokenUseCase.confirm(new ConfirmConvocationByTokenCommand(tokenOf(firstUnitId),
                codeOf(firstUnitId), AttendanceReply.NOT_ATTENDING, "203.0.113.22"));

        assertThat(convocationOf(firstUnitId).attendanceReply()).isEqualTo(AttendanceReply.NOT_ATTENDING);
        assertThat(convocationOf(secondUnitId).attendanceReply()).isEqualTo(AttendanceReply.NO_REPLY);
    }

    @Test
    void changing_ones_mind_through_the_link_is_allowed() {
        // Same rule as everywhere else: the last answer before the session is the one that counts.
        String token = tokenOf(firstUnitId);
        ShortCode code = codeOf(firstUnitId);
        confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, code, AttendanceReply.ATTENDING, "203.0.113.23"));

        ConvocationConfirmationView changed = confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, code, AttendanceReply.NOT_ATTENDING, "203.0.113.23"));

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
                new ConfirmConvocationByTokenCommand("clearly-not-a-real-token", codeOf(firstUnitId),
                        AttendanceReply.ATTENDING, "203.0.113.24")))
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
        ShortCode code = codeOf(firstUnitId);
        openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(meetingId, true));

        assertThatThrownBy(() -> confirmConvocationByTokenUseCase.confirm(
                new ConfirmConvocationByTokenCommand(token, code, AttendanceReply.ATTENDING, "203.0.113.25")))
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
