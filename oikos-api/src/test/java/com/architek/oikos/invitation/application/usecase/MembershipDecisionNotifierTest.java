package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardStaffDirectoryPort;
import com.architek.oikos.invitation.application.port.out.MessagingPort;
import com.architek.oikos.invitation.application.port.out.NotificationPort;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.event.MembershipRequestDecidedEvent;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.WhatsAppSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PhoneNumberVO;
import com.architek.oikos.shared.exception.EmailDeliveryException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MembershipDecisionNotifierTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();
    private static final EntityId UNIT_ID = EntityId.newId();
    private static final EntityId REQUESTER_ID = EntityId.newId();
    private static final EntityId DECIDER_ID = EntityId.newId();

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    @Mock
    private BoardStaffDirectoryPort boardStaffDirectoryPort;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private NotificationPort notificationPort;

    @Mock
    private MessagingPort messagingPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private WhatsAppSenderPort whatsAppSenderPort;

    private MembershipDecisionNotifier newNotifier() {
        return new MembershipDecisionNotifier(accountDirectoryPort, boardStaffDirectoryPort, propertyDirectoryPort,
                unitDirectoryPort, notificationPort, messagingPort, emailSenderPort, whatsAppSenderPort,
                new MembershipDecisionEmailComposer("https://app.example.com"));
    }

    private static MembershipRequestDecidedEvent decided(boolean accepted, String reason) {
        return new MembershipRequestDecidedEvent(MembershipRequestId.newId(), PROPERTY_ID, UNIT_ID, REQUESTER_ID,
                accepted, DECIDER_ID, reason);
    }

    @BeforeEach
    void stubDirectories() {
        when(propertyDirectoryPort.findBasicInfo(PROPERTY_ID))
                .thenReturn(Optional.of(new PropertyBasicInfo("Résidence Al Amal", "12 rue des Orangers")));
        when(unitDirectoryPort.findBasicInfo(UNIT_ID))
                .thenReturn(Optional.of(new UnitBasicInfo(PROPERTY_ID, "A-12", "Appartement", false)));
        when(accountDirectoryPort.getAccountInfo(REQUESTER_ID))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe", "212600000000", true));
        when(accountDirectoryPort.getAccountInfo(DECIDER_ID))
                .thenReturn(new AccountInfo(EmailVO.of("syndic@example.com"), "Karim Syndic", null, true));
        when(boardStaffDirectoryPort.listStaffUserIds(PROPERTY_ID)).thenReturn(List.of(DECIDER_ID));
    }

    /** Les quatre canaux décidés pour le demandeur, sur une seule décision. */
    @Test
    void an_acceptance_reaches_the_requester_on_all_four_channels() {
        newNotifier().announce(decided(true, null));

        verify(notificationPort).notifyRequestDecided(eq(REQUESTER_ID), eq(PROPERTY_ID),
                eq("Votre demande d'adhésion est validée"), any(), eq("/dashboard?space=owner"));
        verify(messagingPort).sendDecisionMessage(eq(PROPERTY_ID), eq(DECIDER_ID), eq(REQUESTER_ID), any(), any());
        verify(emailSenderPort).send(eq(EmailVO.of("jane.doe@example.com")), any(), any());
        verify(whatsAppSenderPort).sendText(eq(PhoneNumberVO.of("212600000000")), any());
    }

    @Test
    void a_rejection_carries_its_reason_to_the_requester() {
        newNotifier().announce(decided(false, "Lot attribué à un autre candidat"));

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(notificationPort).notifyRequestDecided(eq(REQUESTER_ID), eq(PROPERTY_ID),
                eq("Votre demande d'adhésion a été refusée"), body.capture(), any());
        assertThat(body.getValue()).contains("Lot attribué à un autre candidat");
    }

    /** Le décideur vient de cliquer : le prévenir de sa propre décision est du bruit. */
    @Test
    void the_deciding_board_member_is_not_notified_but_the_others_are() {
        EntityId otherStaffId = EntityId.newId();
        when(boardStaffDirectoryPort.listStaffUserIds(PROPERTY_ID)).thenReturn(List.of(DECIDER_ID, otherStaffId));
        newNotifier().announce(decided(true, null));

        verify(notificationPort).notifyRequestDecided(eq(otherStaffId), eq(PROPERTY_ID),
                eq("Demande d'adhésion traitée"), any(), any());
        verify(notificationPort, never()).notifyRequestDecided(eq(DECIDER_ID), any(), any(), any(), any());
    }

    /**
     * Une décision est un fait déjà écrit en base : un SMTP en panne ne doit
     * ni la faire échouer ni emporter les trois autres canaux avec lui.
     */
    @Test
    void a_failing_channel_never_stops_the_others() {
        doThrow(new EmailDeliveryException("SMTP down", new RuntimeException()))
                .when(emailSenderPort).send(any(), any(), any());
        newNotifier().announce(decided(true, null));

        verify(notificationPort).notifyRequestDecided(eq(REQUESTER_ID), any(), any(), any(), any());
        verify(messagingPort).sendDecisionMessage(any(), any(), any(), any(), any());
        verify(whatsAppSenderPort).sendText(any(), any());
    }

    /** Le téléphone est facultatif à l'inscription : ce canal-là saute, les autres partent. */
    @Test
    void a_requester_without_a_phone_number_simply_skips_whatsapp() {
        when(accountDirectoryPort.getAccountInfo(REQUESTER_ID))
                .thenReturn(new AccountInfo(EmailVO.of("jane.doe@example.com"), "Jane Doe", null, true));
        newNotifier().announce(decided(true, null));

        verify(whatsAppSenderPort, never()).sendText(any(), any());
        verify(emailSenderPort).send(any(), any(), any());
    }
}
