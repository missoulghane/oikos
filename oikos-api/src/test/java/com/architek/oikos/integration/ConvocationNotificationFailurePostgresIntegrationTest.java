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
 * The in-app notification is documented as best effort in two places. This is
 * what makes that true rather than merely written down.
 *
 * <p>It was not, and the failure mode was invisible in every cheaper test. The
 * notification's own services (FindUsersByPartyIdsService,
 * CreateNotificationService) are {@code @Transactional} with default REQUIRED
 * propagation, so they joined the sending transaction; a throw among them marked
 * that transaction rollback-only before the catch in SendConvocationService ever
 * saw the exception. The catch then swallowed it, the commit failed with
 * UnexpectedRollbackException, and the convocation came back out as TO_SEND -
 * with the email already delivered. A syndic would re-send it, and the
 * copropriétaire would receive it twice.
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
        // document.uploaded_by is a foreign key onto app_user.
        jdbcTemplate.update("insert into app_user (id, email, full_name, password_hash, verified, enabled, "
                        + "created_date, last_modified_date, version) values (?, ?, ?, 'x', true, true, ?, ?, 0)",
                syndicUserId.value(), "syndic." + unique + "@example.com", "Syndic", now, now);

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

    @Test
    void a_notification_that_blows_up_does_not_undo_the_send() {
        doThrow(new IllegalStateException("notification service is down"))
                .when(createNotificationUseCase).create(any());

        SendPendingConvocationsResult result = sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.EMAIL, syndicUserId));

        // The convocation left, and the record says so. Before the dispatch was isolated in
        // its own transaction this came back sentCount=0 / failedCount=1, with the email gone.
        assertThat(result.sentCount()).isEqualTo(1);
        assertThat(result.failedCount()).isZero();
        assertThat(convocation().deliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(convocation().sentAt()).isNotNull();
    }

    @Test
    void the_filed_pdf_survives_a_failing_notification_too() {
        // The document is written before the notification is attempted, so it is the clearest
        // witness of the rollback that used to take the whole transaction with it.
        doThrow(new IllegalStateException("notification service is down"))
                .when(createNotificationUseCase).create(any());

        sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.EMAIL, syndicUserId));

        Long documents = jdbcTemplate.queryForObject(
                "select count(*) from document where owner_type = 'CONVOCATION' and owner_id = ?", Long.class,
                convocation().id().value().value());
        assertThat(documents).isEqualTo(1L);
    }

    @Test
    void a_working_notification_is_still_recorded() {
        // The other half: isolating the dispatch must not have quietly disconnected it.
        sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(meetingId, ChannelCode.EMAIL, syndicUserId));

        Long notifications = jdbcTemplate.queryForObject(
                "select count(*) from notification where property_id = ? and type = 'GENERAL_MEETING_CALLED'",
                Long.class, propertyId.value());
        assertThat(notifications).isEqualTo(1L);
    }
}
