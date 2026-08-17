package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.SendPendingConvocationsCommand;
import com.architek.oikos.meeting.application.dto.ConvocationDeliveryView;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.SendPendingConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.SendPendingConvocationsUseCase.SendPendingConvocationsResult;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.notification.application.port.in.CreateNotificationUseCase;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * What survives a notification that blows up, and what each channel does on its
 * own. Two concerns in one fixture because they share the same seam: the
 * dispatch's transaction.
 *
 * <p>The historical defect: the notification's own services
 * (FindUsersByPartyIdsService, CreateNotificationService) are
 * {@code @Transactional} with default REQUIRED propagation, so they joined the
 * sending transaction; a throw among them marked it rollback-only before the
 * catch in SendConvocationService ever saw the exception. The catch swallowed
 * it, the commit failed with UnexpectedRollbackException, and the convocation
 * came back out as TO_SEND. MeetingInAppDispatcher's REQUIRES_NEW is
 * what fixes it, and TO_SEND is still the symptom these tests watch for.
 *
 * <p>What changed since: the send is performed one channel at a time, so the
 * notification is the delivery on the APP channel rather than a courtesy
 * alongside every email. The tests therefore drive the failure through APP -
 * where a failing notification now legitimately produces FAILED rather than
 * being swallowed - and one of them pins the separation itself: an EMAIL send
 * notifies nobody.
 *
 * <p>Only a real transaction manager on a real database shows any of this: with
 * a mocked repository there is no commit to fail. That is the same reason
 * ConvocationDispatchPostgresIntegrationTest exists, one layer along.
 *
 * <p>MockitoSpyBean rather than MockitoBean: the rest of the context keeps the
 * real notification service, and only the one call this test is about is made to
 * blow up.
 */
@TestPropertySource(properties = "oikos.mail.enabled=false")
class ConvocationNotificationFailurePostgresIntegrationTest extends PostgresIntegrationTestBase {

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
    private SendPendingConvocationsUseCase sendPendingConvocationsUseCase;

    @Autowired
    private ListConvocationsByMeetingUseCase listConvocationsByMeetingUseCase;

    @MockitoSpyBean
    private CreateNotificationUseCase createNotificationUseCase;

    private EntityId propertyId;
    private UUID unitId;
    private EntityId syndicUserId;
    private GeneralMeetingId meetingId;

    @BeforeEach
    void seedALotWhoseOwnerHasAnAccount() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String unique = UUID.randomUUID().toString();
        propertyId = EntityId.of(UUID.randomUUID());
        UUID buildingId = UUID.randomUUID();
        UUID unitTypeId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        unitId = UUID.randomUUID();
        syndicUserId = EntityId.of(UUID.randomUUID());

        jdbcTemplate.update("insert into property (id, name, address, created_date, last_modified_date, version, "
                        + "dues_calculation_mode) values (?, ?, ?, ?, ?, 0, 'SHARES')",
                propertyId.value(), "Résidence Al Amal", "12 rue des Orangers", now, now);
        jdbcTemplate.update("insert into building (id, property_id, name, floor_count, created_date, "
                        + "last_modified_date, version) values (?, ?, ?, 4, ?, ?, 0)",
                buildingId, propertyId.value(), "Bâtiment A", now, now);
        jdbcTemplate.update("insert into unit_type_definition (id, property_id, name, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Appartement', ?, ?, 0)",
                unitTypeId, propertyId.value(), now, now);
        jdbcTemplate.update("insert into unit (id, building_id, property_id, unit_number, unit_type_id, shares, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, ?, ?, ?, ?, 0)",
                unitId, buildingId, propertyId.value(), "Appartement 1", unitTypeId, new BigDecimal("120.00"), now,
                now);

        // An owner with an email AND an account: both halves are needed, or the send never
        // reaches the notification at all and the test would pass for the wrong reason.
        jdbcTemplate.update("insert into party (id, property_id, full_name, party_type, email, created_date, "
                        + "last_modified_date, version) values (?, ?, ?, 'INDIVIDUAL', ?, ?, ?, 0)",
                partyId, propertyId.value(), "Karim Benali", "karim." + unique + "@example.com", now, now);
        jdbcTemplate.update("insert into unit_ownership (id, unit_id, party_id, property_id, ownership_share, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, ?, 100.00, ?, ?, 0)",
                UUID.randomUUID(), unitId, partyId, propertyId.value(), now, now);
        jdbcTemplate.update("insert into app_user (id, email, full_name, password_hash, verified, enabled, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, 'x', true, true, ?, ?, 0)",
                ownerUserId, "karim.user." + unique + "@example.com", "Karim Benali", now, now);
        jdbcTemplate.update("insert into user_role (user_id, role) values (?, 'ROLE_USER')", ownerUserId);
        jdbcTemplate.update("insert into app_user_party (app_user_id, party_id) values (?, ?)", ownerUserId, partyId);
        // The role grant, not just the account link: the messagerie only accepts recipients who
        // are members of the property, where the notification only needed a linked account. A
        // convocation delivered in-app therefore reaches a narrower population than the bell
        // alone used to - deliberately, since somebody with no membership sees no messagerie.
        jdbcTemplate.update("insert into app_user_party_role (app_user_id, party_id, property_id, role) "
                        + "values (?, ?, ?, 'PROPERTY_OWNER')", ownerUserId, partyId, propertyId.value());

        // document.uploaded_by is a foreign key onto app_user.
        jdbcTemplate.update("insert into app_user (id, email, full_name, password_hash, verified, enabled, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, 'x', true, true, ?, ?, 0)",
                syndicUserId.value(), "syndic." + unique + "@example.com", "Syndic", now, now);
        // The syndic is the sender of the messagerie thread - the messagerie has no system
        // sender, and a convocation attributed to nobody would leave a thread nobody can answer.
        // A manager role, so SenderIdentity.BOARD is one the sender actually holds.
        UUID syndicPartyId = UUID.randomUUID();
        jdbcTemplate.update("insert into party (id, property_id, full_name, party_type, email, created_date, "
                        + "last_modified_date, version) values (?, ?, 'Cabinet Syndic', 'COMPANY', ?, ?, ?, 0)",
                syndicPartyId, propertyId.value(), "cabinet." + unique + "@example.com", now, now);
        // Without a global role the user module refuses to reconstruct the account, and the
        // messagerie resolves the sender through it.
        jdbcTemplate.update("insert into user_role (user_id, role) values (?, 'ROLE_USER')", syndicUserId.value());
        jdbcTemplate.update("insert into app_user_party (app_user_id, party_id) values (?, ?)",
                syndicUserId.value(), syndicPartyId);
        jdbcTemplate.update("insert into app_user_party_role (app_user_id, party_id, property_id, role) "
                        + "values (?, ?, ?, 'PROPERTY_MANAGER_ADMIN')", syndicUserId.value(), syndicPartyId,
                propertyId.value());

        meetingId = createGeneralMeetingUseCase.create(new CreateGeneralMeetingCommand(propertyId,
                MeetingType.ORDINARY, "AG ordinaire 2026", SESSION_DATE, VENUE));
        addAgendaItemUseCase.add(new AddAgendaItemCommand(meetingId, "Approbation des comptes 2025", null,
                MajorityRule.SIMPLE));
        scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(meetingId, SESSION_DATE, VENUE));
        generateConvocationsUseCase.generate(new GenerateConvocationsCommand(meetingId));
    }

    private ConvocationView convocation() {
        return listConvocationsByMeetingUseCase
                .listConvocations(new ListConvocationsByMeetingQuery(meetingId, null)).stream()
                .filter(view -> view.unitId().value().equals(unitId)).findFirst().orElseThrow();
    }

    private Long notificationCount() {
        return jdbcTemplate.queryForObject(
                "select count(*) from notification where property_id = ? and type = 'GENERAL_MEETING_CALLED'",
                Long.class, propertyId.value());
    }

    @Test
    void a_notification_that_blows_up_leaves_a_failed_delivery_behind() {
        doThrow(new IllegalStateException("notification service is down"))
                .when(createNotificationUseCase).create(any());

        SendPendingConvocationsResult result = sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.APP, syndicUserId));

        // FAILED, and above all not TO_SEND. TO_SEND is the rollback symptom this whole test
        // exists for: the notification's services joining the sending transaction, marking it
        // rollback-only, and taking the record of the attempt with them on the way out. The
        // syndic would then see a lot nobody had ever tried, rather than one to reach some
        // other way.
        assertThat(result.sentCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(convocation().deliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(convocation().deliveries()).singleElement()
                .satisfies(delivery -> assertThat(delivery.channelCode()).isEqualTo("APP"));
    }

    @Test
    void the_filed_pdf_survives_a_failing_notification_too() {
        // The document is written before the notification is attempted, so it is the clearest
        // witness of the rollback that used to take the whole transaction with it.
        doThrow(new IllegalStateException("notification service is down"))
                .when(createNotificationUseCase).create(any());

        sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.APP, syndicUserId));

        Long documents = jdbcTemplate.queryForObject(
                "select count(*) from document where owner_type = 'CONVOCATION' and owner_id = ?", Long.class,
                convocation().id().value().value());
        assertThat(documents).isEqualTo(1L);
    }

    @Test
    void a_working_notification_is_still_recorded() {
        // The other half: isolating the dispatch must not have quietly disconnected it.
        SendPendingConvocationsResult result = sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.APP, syndicUserId));

        assertThat(result.sentCount()).isEqualTo(1);
        assertThat(convocation().deliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(notificationCount()).isEqualTo(1L);
    }

    @Test
    void an_app_send_leaves_a_message_in_the_messagerie_and_one_delivery() {
        // The channel is the messagerie now, not only the bell: reaching a lot inside the
        // application means leaving its owners something they can read, not just an alert.
        SendPendingConvocationsResult result = sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.APP, syndicUserId));

        assertThat(result.sentCount()).isEqualTo(1);
        Long messages = jdbcTemplate.queryForObject(
                "select count(*) from message m join conversation c on c.id = m.conversation_id "
                        + "where c.property_id = ?", Long.class, propertyId.value());
        assertThat(messages).isEqualTo(1L);
        // One act, one row - ADR 0002 §9 refused a second channel on exactly this ground:
        // two rows would count two envois for one act and show 120 for 60 lots.
        assertThat(convocation().deliveries()).singleElement()
                .satisfies(delivery -> assertThat(delivery.channelCode()).isEqualTo("APP"));
        assertThat(notificationCount()).isEqualTo(1L);
    }

    @Test
    void the_thread_names_the_lot_it_convokes() {
        // An owner of three lots gets three threads, and "concerne" is what tells them apart.
        sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.APP, syndicUserId));

        String concerns = jdbcTemplate.queryForObject(
                "select concerns_unit from conversation where property_id = ?", String.class, propertyId.value());
        assertThat(concerns).isEqualTo("Bâtiment A - Appartement 1");
    }

    @Test
    void an_email_send_leaves_no_message_either() {
        sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.EMAIL, syndicUserId));

        Long messages = jdbcTemplate.queryForObject(
                "select count(*) from message m join conversation c on c.id = m.conversation_id "
                        + "where c.property_id = ?", Long.class, propertyId.value());
        assertThat(messages).isZero();
    }

    @Test
    void an_email_send_notifies_nobody_and_says_only_email() {
        // The defect that made the per-channel split necessary: the service mailed AND notified
        // on every call, then recorded one row carrying whichever code was asked for. Sending
        // "by APP" wrote APP while the email went out too, so the tracking table asserted a
        // route nobody had taken. One channel is now one act and one row.
        SendPendingConvocationsResult result = sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.EMAIL, syndicUserId));

        assertThat(result.sentCount()).isEqualTo(1);
        assertThat(convocation().deliveries()).singleElement()
                .satisfies(delivery -> assertThat(delivery.channelCode()).isEqualTo("EMAIL"));
        assertThat(notificationCount()).isZero();
    }

    @Test
    void each_channel_keeps_its_own_population_to_send_to() {
        // "Pending" is now asked of a channel, not of the convocation. Under the old reading a
        // successful email run emptied every other channel: press "Email", then "Messagerie",
        // and the second button silently found nobody left.
        sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.EMAIL, syndicUserId));
        SendPendingConvocationsResult byApp = sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.APP, syndicUserId));

        assertThat(byApp.sentCount()).isEqualTo(1);
        assertThat(convocation().deliveries()).extracting(ConvocationDeliveryView::channelCode)
                .containsExactly("EMAIL", "APP");
        // Still the date of the FIRST success, whatever the second channel did afterwards.
        assertThat(convocation().sentAt()).isNotNull();
    }
}
