package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.RecordConvocationDeliveryCommand;
import com.architek.oikos.meeting.application.command.ReplyToConvocationCommand;
import com.architek.oikos.meeting.application.command.SendPendingConvocationsCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.UpdateGeneralMeetingCommentCommand;
import com.architek.oikos.meeting.application.dto.AttendanceSummaryView;
import com.architek.oikos.meeting.application.dto.ConvocationDeliveryView;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.GetAttendanceSummaryUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationChannelsUseCase;
import com.architek.oikos.meeting.application.port.in.OpenGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.RecordConvocationDeliveryUseCase;
import com.architek.oikos.meeting.application.port.in.ReplyToConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationDocumentUseCase;
import com.architek.oikos.meeting.application.port.in.SendPendingConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.UpdateGeneralMeetingCommentUseCase;
import com.architek.oikos.meeting.application.query.GetAttendanceSummaryQuery;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.domain.exception.ConvocationChannelNotFoundException;
import com.architek.oikos.meeting.domain.exception.ConvocationNotSendableException;
import com.architek.oikos.meeting.domain.exception.QuorumNotReachedException;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.shared.domain.valueobject.EntityId;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The convocation run end to end, on a real PostgreSQL: generate, then send,
 * render the PDF, file it through the document module.
 *
 * <p>Generating and sending are two separate actions, and this pins that they
 * stay separate: generating must leave every convocation TO_SEND.
 *
 * <p>This is also the only level at which the sending can be trusted. Each
 * send runs in a REQUIRES_NEW transaction with noRollbackFor on the
 * unreachable-lot case; get either wrong and the writes are discarded
 * silently, with no exception to catch - the tracking table then shows TO_SEND
 * for convocations that really went out, or loses the FAILED mark that tells
 * the syndic to post a letter. A mocked unit test cannot see any of it (that is
 * the trap GeneratePaymentReceiptService documents, and
 * PaymentReceiptGenerationIntegrationTest guards for receipts).
 *
 * <p>Emails are logged, not sent: oikos.mail.enabled=false selects
 * LoggingEmailAdapter.
 */
@TestPropertySource(properties = "oikos.mail.enabled=false")
class ConvocationDispatchPostgresIntegrationTest extends PostgresIntegrationTestBase {

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
    private GetAttendanceSummaryUseCase getAttendanceSummaryUseCase;

    @Autowired
    private OpenGeneralMeetingUseCase openGeneralMeetingUseCase;

    @Autowired
    private SendPendingConvocationsUseCase sendPendingConvocationsUseCase;

    @Autowired
    private GetConvocationDocumentUseCase getConvocationDocumentUseCase;

    @Autowired
    private RecordConvocationDeliveryUseCase recordConvocationDeliveryUseCase;

    @Autowired
    private ReplyToConvocationUseCase replyToConvocationUseCase;

    @Autowired
    private ListConvocationChannelsUseCase listConvocationChannelsUseCase;

    @Autowired
    private UpdateGeneralMeetingCommentUseCase updateGeneralMeetingCommentUseCase;

    private EntityId propertyId;
    private UUID ownedUnitId;
    private UUID unownedUnitId;
    private EntityId syndicUserId;
    private EntityId ownerUserId;
    private UUID ownerPartyId;
    private GeneralMeetingId meetingId;

    /**
     * The fixture is written straight to the tables rather than through the
     * property module's use cases: what is under test here is the convocation
     * run, and a lot with no owner at all - the case that matters most for the
     * totals - is awkward to reach through those use cases.
     */
    @BeforeEach
    void seedPropertyAndScheduledMeeting() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        // The container is a JVM-wide singleton and nothing rolls back between methods, so
        // every unique column has to be unique per test, not per class.
        String unique = UUID.randomUUID().toString();
        propertyId = EntityId.of(UUID.randomUUID());
        UUID buildingId = UUID.randomUUID();
        UUID unitTypeId = UUID.randomUUID();
        ownerPartyId = UUID.randomUUID();
        ownedUnitId = UUID.randomUUID();
        unownedUnitId = UUID.randomUUID();
        syndicUserId = EntityId.of(UUID.randomUUID());
        ownerUserId = EntityId.of(UUID.randomUUID());

        jdbcTemplate.update("insert into property (id, name, address, created_date, last_modified_date, version, "
                        + "dues_calculation_mode) values (?, ?, ?, ?, ?, 0, 'SHARES')",
                propertyId.value(), "Résidence Al Amal", "12 rue des Orangers", now, now);
        jdbcTemplate.update("insert into building (id, property_id, name, floor_count, created_date, "
                        + "last_modified_date, version) values (?, ?, ?, 4, ?, ?, 0)",
                buildingId, propertyId.value(), "Bâtiment A", now, now);
        jdbcTemplate.update("insert into unit_type_definition (id, property_id, name, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Appartement', ?, ?, 0)",
                unitTypeId, propertyId.value(), now, now);
        insertUnit(ownedUnitId, buildingId, unitTypeId, "Appartement 1", new BigDecimal("120.00"), now);
        insertUnit(unownedUnitId, buildingId, unitTypeId, "Appartement 2", new BigDecimal("80.00"), now);

        jdbcTemplate.update("insert into party (id, property_id, full_name, party_type, email, created_date, "
                        + "last_modified_date, version) values (?, ?, ?, 'INDIVIDUAL', ?, ?, ?, 0)",
                ownerPartyId, propertyId.value(), "Karim Benali", "karim." + unique + "@example.com", now, now);
        jdbcTemplate.update("insert into unit_ownership (id, unit_id, party_id, property_id, ownership_share, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, 100.00, ?, ?, 0)",
                UUID.randomUUID(), ownedUnitId, ownerPartyId, propertyId.value(), now, now);
        // document.uploaded_by is a foreign key onto app_user - the PDF has to be attributed
        // to a real account or the upload fails at flush time.
        jdbcTemplate.update("insert into app_user (id, email, full_name, password_hash, verified, enabled, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, 'x', true, true, ?, ?, 0)",
                syndicUserId.value(), "syndic." + unique + "@example.com", "Syndic", now, now);
        // The lot's owner, with an account of their own: what tells an owner answering for
        // themselves from a syndic entering an answer they were given over the phone.
        jdbcTemplate.update("insert into app_user (id, email, full_name, password_hash, verified, enabled, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, 'x', true, true, ?, ?, 0)",
                ownerUserId.value(), "karim.user." + unique + "@example.com", "Karim Benali", now, now);
        // A user with no role cannot be reconstructed by the user module, and the in-app
        // notification resolves this account through it during the send.
        jdbcTemplate.update("insert into user_role (user_id, role) values (?, 'ROLE_USER')", ownerUserId.value());
        jdbcTemplate.update("insert into app_user_party (app_user_id, party_id) values (?, ?)",
                ownerUserId.value(), ownerPartyId);

        meetingId = createGeneralMeetingUseCase.create(
                new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG ordinaire 2026", SESSION_DATE, VENUE));
        addAgendaItemUseCase.add(new AddAgendaItemCommand(meetingId, "Approbation des comptes 2025", null,
                MajorityRule.ABSOLUTE));
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(meetingId, SESSION_DATE, VENUE));
    }

    private void insertUnit(UUID unitId, UUID buildingId, UUID unitTypeId, String unitNumber, BigDecimal shares,
                             OffsetDateTime now) {
        jdbcTemplate.update("insert into unit (id, building_id, property_id, unit_number, unit_type_id, shares, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                unitId, buildingId, propertyId.value(), unitNumber, unitTypeId, shares, now, now);
    }

    private List<ConvocationView> generate() {
        return generateConvocationsUseCase.generate(new GenerateConvocationsCommand(meetingId));
    }

    private void sendPending() {
        sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.EMAIL, syndicUserId));
    }

    private void generateAndSend() {
        generate();
        sendPending();
    }

    private List<ConvocationView> currentConvocations() {
        return listConvocationsByMeetingUseCase.listConvocations(
                new ListConvocationsByMeetingQuery(meetingId, null));
    }

    @Test
    void every_lot_is_convoked_including_the_one_nobody_owns() {
        generate();

        assertThat(currentConvocations()).extracting(view -> view.unitId().value())
                .containsExactlyInAnyOrder(ownedUnitId, unownedUnitId);
    }

    @Test
    void generating_sends_nothing() {
        // The two actions are separate: generating convokes the meeting and leaves every
        // convocation waiting. Welding them back together would show up here.
        generate();

        assertThat(currentConvocations())
                .allSatisfy(view -> assertThat(view.deliveryStatus()).isEqualTo(DeliveryStatus.TO_SEND));
    }

    @Test
    void sending_really_persists_what_it_did() {
        // With the wrong propagation these writes vanish silently and the assertions
        // below still find TO_SEND.
        generateAndSend();

        ConvocationView owned = convocationOf(ownedUnitId);
        assertThat(owned.deliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(owned.sentAt()).isNotNull();
        assertThat(owned.deliveries()).singleElement()
                .satisfies(delivery -> assertThat(delivery.channelCode()).isEqualTo("EMAIL"));
        assertThat(owned.status()).isEqualTo(ConvocationStatus.SENT);
    }

    @Test
    void the_letter_can_be_downloaded_before_anything_is_sent() {
        // What makes the postal flow workable at all: generate, print, post. Rendering only
        // at send time left "imprimez les convocations générées" with nothing to print.
        generate();

        GetConvocationDocumentUseCase.ConvocationDocument letter = getConvocationDocumentUseCase.getDocument(
                new GetConvocationQuery(ConvocationId.of(convocationOf(ownedUnitId).id().toString())));

        assertThat(letter.alreadySent()).isFalse();
        assertThat(letter.fileName()).isEqualTo("convocation-appartement-1.pdf");
        assertThat(letter.content()).isNotEmpty();
        // A real PDF, not an error page rendered to bytes.
        assertThat(new String(letter.content(), 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void once_sent_the_download_returns_the_filed_letter_rather_than_a_re_render() {
        generateAndSend();

        GetConvocationDocumentUseCase.ConvocationDocument letter = getConvocationDocumentUseCase.getDocument(
                new GetConvocationQuery(ConvocationId.of(convocationOf(ownedUnitId).id().toString())));

        assertThat(letter.alreadySent()).isTrue();
        assertThat(letter.content()).isNotEmpty();
    }

    @Test
    void the_convocation_pdf_is_filed_through_the_document_module() {
        generateAndSend();

        Long documents = jdbcTemplate.queryForObject(
                "select count(*) from document where owner_type = 'CONVOCATION' and owner_id = ?", Long.class,
                convocationOf(ownedUnitId).id().value().value());
        assertThat(documents).isEqualTo(1L);
    }

    @Test
    void a_lot_with_nobody_to_write_to_is_recorded_as_failed_rather_than_left_pending() {
        // An unreachable lot is exactly what the syndic must see, to convoke it by post.
        generateAndSend();

        assertThat(convocationOf(unownedUnitId).deliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    void one_unreachable_lot_does_not_stop_the_others() {
        generateAndSend();

        assertThat(convocationOf(ownedUnitId).deliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(convocationOf(unownedUnitId).deliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    void generating_twice_neither_duplicates_a_convocation_nor_resends_it() {
        generateAndSend();
        Instant firstSentAt = convocationOf(ownedUnitId).sentAt();

        generateAndSend();

        assertThat(currentConvocations()).hasSize(2);
        assertThat(convocationOf(ownedUnitId).sentAt()).isEqualTo(firstSentAt);
    }

    @Test
    void the_attendance_summary_counts_both_lots_and_all_their_weight() {
        generateAndSend();

        AttendanceSummaryView summary = getAttendanceSummaryUseCase.getSummary(
                new GetAttendanceSummaryQuery(meetingId));

        assertThat(summary.totalUnits()).isEqualTo(2);
        assertThat(summary.totalWeight()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(summary.presentWeight()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.noReplyCount()).isEqualTo(2);
        // No threshold configured for this property: the check passes, and says so.
        assertThat(summary.quorumRequired()).isFalse();
        assertThat(summary.quorumReached()).isTrue();
    }

    @Test
    void the_session_opens_once_the_convocations_exist() {
        generate();
        // No send needed: convoking is what opens the door to the session.

        assertThat(openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(meetingId, false)).status())
                .isEqualTo(MeetingStatus.IN_PROGRESS);
    }

    @Test
    void a_configured_quorum_blocks_the_opening_of_an_empty_room_unless_it_is_forced() {
        jdbcTemplate.update("insert into meeting_quorum_setting (id, property_id, meeting_type, quorum_percentage, "
                        + "created_date, last_modified_date, version) values (?, ?, 'ORDINARY', 50.00, ?, ?, 0)",
                UUID.randomUUID(), propertyId.value(), OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC));
        GeneralMeetingId quorateMeeting = createGeneralMeetingUseCase.create(
                new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG avec quorum", SESSION_DATE, VENUE));
        addAgendaItemUseCase.add(new AddAgendaItemCommand(quorateMeeting, "Point", null, MajorityRule.SIMPLE));
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(quorateMeeting, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(new GenerateConvocationsCommand(quorateMeeting));

        assertThatThrownBy(() -> openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(quorateMeeting, false)))
                .isInstanceOf(QuorumNotReachedException.class);

        assertThat(openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(quorateMeeting, true))
                .openedWithoutQuorum()).isTrue();
    }

    @Test
    void the_channel_catalog_is_seeded_by_the_migration_and_says_which_ones_are_automated() {
        // The list is data now: this is the query the select box on the sending screen runs,
        // and an empty catalog would make every convocation unsendable.
        assertThat(listConvocationChannelsUseCase.listChannels())
                .extracting(channel -> channel.getCode().value())
                .containsExactly("EMAIL", "APP", "POSTAL_MAIL", "REGISTERED_MAIL", "MANUAL");

        assertThat(listConvocationChannelsUseCase.listChannels())
                .filteredOn(com.architek.oikos.meeting.domain.model.ConvocationChannel::isAutomated)
                .extracting(channel -> channel.getCode().value())
                .containsExactly("EMAIL", "APP");
    }

    @Test
    void a_registered_letter_recorded_after_an_email_does_not_erase_it() {
        // The change this whole lot exists for. The previous shape held one channel and one
        // date, so recording the letter wiped the record of the email.
        generateAndSend();
        ConvocationView emailed = convocationOf(ownedUnitId);
        Instant emailSentAt = emailed.sentAt();

        recordConvocationDeliveryUseCase.record(new RecordConvocationDeliveryCommand(
                ConvocationId.of(emailed.id().toString()), ChannelCode.of("REGISTERED_MAIL"), DeliveryStatus.SENT,
                "RR-2026-0042", syndicUserId));

        ConvocationView twice = convocationOf(ownedUnitId);
        assertThat(twice.deliveries()).extracting(ConvocationDeliveryView::channelCode)
                .containsExactly("EMAIL", "REGISTERED_MAIL");
        assertThat(twice.deliveries()).extracting(ConvocationDeliveryView::channelLabel)
                .containsExactly("Email", "Courrier recommandé avec AR");
        assertThat(twice.deliveries().get(1).reference()).isEqualTo("RR-2026-0042");
        // And the date the notice period runs from is still the first one.
        assertThat(twice.sentAt()).isEqualTo(emailSentAt);
    }

    @Test
    void a_letter_posted_to_a_lot_no_email_could_reach_makes_it_reached() {
        // The postal fallback, which is the reason the failure is recorded rather than dropped:
        // one success on any channel is enough, and the failed attempt stays visible.
        generateAndSend();
        ConvocationView unreachable = convocationOf(unownedUnitId);
        assertThat(unreachable.deliveryStatus()).isEqualTo(DeliveryStatus.FAILED);

        recordConvocationDeliveryUseCase.record(new RecordConvocationDeliveryCommand(
                ConvocationId.of(unreachable.id().toString()), ChannelCode.of("POSTAL_MAIL"), DeliveryStatus.SENT, null,
                syndicUserId));

        ConvocationView posted = convocationOf(unownedUnitId);
        assertThat(posted.deliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(posted.status()).isEqualTo(ConvocationStatus.SENT);
        assertThat(posted.deliveries()).extracting(ConvocationDeliveryView::status)
                .containsExactly(DeliveryStatus.FAILED, DeliveryStatus.SENT);
    }

    @Test
    void the_application_refuses_to_send_by_a_channel_a_person_has_to_perform() {
        // Pressing a button must never be able to claim a letter left the building.
        generate();

        assertThatThrownBy(() -> sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.of("POSTAL_MAIL"), syndicUserId)))
                .isInstanceOf(ConvocationNotSendableException.class);
    }

    @Test
    void a_channel_that_is_in_no_catalog_row_is_refused_outright() {
        // Since the codes are data, a client can invent one - it must not reach the sending path.
        generate();

        assertThatThrownBy(() -> sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.of("CARRIER_PIGEON"), syndicUserId)))
                .isInstanceOf(ConvocationChannelNotFoundException.class);
    }

    @Test
    void an_answer_entered_by_the_syndic_says_so_rather_than_passing_for_the_owner() {
        // The provenance is deduced from the caller: the syndic does not own this lot, so
        // whatever they type, the answer is recorded as taken at the office.
        generateAndSend();

        ConvocationView answered = replyToConvocationUseCase.reply(new ReplyToConvocationCommand(
                ConvocationId.of(convocationOf(ownedUnitId).id().toString()), AttendanceReply.ATTENDING,
                "appelée mardi", syndicUserId));

        assertThat(answered.replySource()).isEqualTo(ReplySource.SYNDIC_OFFICE);
        assertThat(answered.replyNote()).isEqualTo("appelée mardi");
        assertThat(answered.status()).isEqualTo(ConvocationStatus.CONFIRMED);
    }

    @Test
    void an_owner_answering_for_their_own_lot_is_recorded_as_such_and_named() {
        // Deduced, never declared: the same endpoint and the same body yield OWNER_APP here and
        // SYNDIC_OFFICE above, purely because of who is calling.
        generateAndSend();

        ConvocationView answered = replyToConvocationUseCase.reply(new ReplyToConvocationCommand(
                ConvocationId.of(convocationOf(ownedUnitId).id().toString()), AttendanceReply.ATTENDING, null,
                ownerUserId));

        assertThat(answered.replySource()).isEqualTo(ReplySource.OWNER_APP);
        assertThat(jdbcTemplate.queryForObject("select replied_by_party_id from convocation where id = ?", UUID.class,
                answered.id().value().value())).isEqualTo(ownerPartyId);
    }

    @Test
    void withdrawing_an_answer_takes_its_provenance_with_it() {
        generateAndSend();
        ConvocationId convocationId = ConvocationId.of(convocationOf(ownedUnitId).id().toString());
        replyToConvocationUseCase.reply(new ReplyToConvocationCommand(convocationId, AttendanceReply.ATTENDING, null,
                syndicUserId));

        ConvocationView withdrawn = replyToConvocationUseCase.reply(new ReplyToConvocationCommand(convocationId,
                AttendanceReply.NO_REPLY, null, syndicUserId));

        assertThat(withdrawn.replySource()).isNull();
        assertThat(withdrawn.repliedAt()).isNull();
        assertThat(withdrawn.status()).isEqualTo(ConvocationStatus.SENT);
    }

    @Test
    void the_syndics_note_reaches_the_letter_as_readable_text() {
        // The comment is rich text, but openhtmltopdf runs here without an HTML5 parser: the
        // letter takes RichTextToParagraphs' plain text. A <br> reaching the template raw is
        // not a cosmetic defect, it is a convocation that fails to render at all.
        updateGeneralMeetingCommentUseCase.updateComment(new UpdateGeneralMeetingCommentCommand(meetingId,
                "<p>Le budget <strong>2026</strong> intègre le ravalement.</p><p>Les devis sont joints.<br>Merci.</p>"));
        generate();

        GetConvocationDocumentUseCase.ConvocationDocument letter = getConvocationDocumentUseCase.getDocument(
                new GetConvocationQuery(ConvocationId.of(convocationOf(ownedUnitId).id().toString())));

        assertThat(letter.content()).isNotEmpty();
        assertThat(new String(letter.content(), 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void a_meeting_without_a_note_still_produces_its_letter() {
        generate();

        GetConvocationDocumentUseCase.ConvocationDocument letter = getConvocationDocumentUseCase.getDocument(
                new GetConvocationQuery(ConvocationId.of(convocationOf(ownedUnitId).id().toString())));

        assertThat(new String(letter.content(), 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void an_empty_note_is_stored_as_nothing_rather_than_as_an_empty_editor() {
        // Quill's "empty" document is "<p><br></p>", and the API normalises blank to null so
        // that every reader has one absence to check for rather than three.
        updateGeneralMeetingCommentUseCase.updateComment(new UpdateGeneralMeetingCommentCommand(meetingId, "   "));

        assertThat(jdbcTemplate.queryForObject("select comment from general_meeting where id = ?", String.class,
                meetingId.asUuid())).isNull();
    }

    private ConvocationView convocationOf(UUID unitId) {
        return currentConvocations().stream().filter(view -> view.unitId().value().equals(unitId)).findFirst()
                .orElseThrow();
    }
}
