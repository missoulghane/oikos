package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.architek.oikos.invitation.application.command.CreateInvitationCommand;
import com.architek.oikos.invitation.application.port.out.PartyContactInfo;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.PublicInvitationAlreadyExistsException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.service.InvitationTokenGenerator;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateInvitationServiceTest {

    private static final EmailVO TARGET_EMAIL = EmailVO.of("jane.doe@example.com");

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    private CreateInvitationService newService() {
        return new CreateInvitationService(invitationRepository, propertyDirectoryPort, unitDirectoryPort,
                partyDirectoryPort, new InvitationTokenGenerator(),
                new InvitationLinkComposer("https://app.example.com/invitations"),
                new OwnerInvitationEmailComposer(), emailSenderPort,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 30L);
    }

    private EntityId knownContact(EntityId propertyId) {
        EntityId partyId = EntityId.newId();
        when(partyDirectoryPort.findById(partyId))
                .thenReturn(Optional.of(new PartyContactInfo(propertyId, "Jane Doe", TARGET_EMAIL)));
        return partyId;
    }

    private void knownProperty(EntityId propertyId) {
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void knownUnit(EntityId propertyId, EntityId unitId) {
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A-12", "Appartement", false)));
    }

    private Invitation captureSaved() {
        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void creating_a_public_invitation_for_a_known_property_persists_it() {
        EntityId propertyId = EntityId.newId();
        knownProperty(propertyId);

        newService().create(new CreateInvitationCommand(propertyId, InvitationType.PUBLIC, null, null, EntityId.newId()));

        Invitation saved = captureSaved();
        assertThat(saved.getType()).isEqualTo(InvitationType.PUBLIC);
        assertThat(saved.getTargetRole()).isEqualTo("PROPERTY_OWNER");
        assertThat(saved.getTargetUnitId()).isNull();
        // Personne à qui l'envoyer : un lien public se partage, il ne s'adresse pas.
        verify(emailSenderPort, never()).send(any(), any(), any());
    }

    /**
     * Un second lien public invaliderait silencieusement le premier - celui
     * dont le QR code est imprimé dans le hall. On le rouvre, on ne le
     * remplace pas (voir EnableInvitationService).
     */
    @Test
    void creating_a_second_public_invitation_for_the_same_property_is_refused() {
        EntityId propertyId = EntityId.newId();
        knownProperty(propertyId);
        when(invitationRepository.findPublicByPropertyId(propertyId)).thenReturn(Optional.of(
                Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PUBLIC, "PROPERTY_OWNER", null,
                        "tok", Instant.EPOCH.plus(Duration.ofDays(30)), EntityId.newId(), null, null, null)));

        assertThatThrownBy(() -> newService().create(
                new CreateInvitationCommand(propertyId, InvitationType.PUBLIC, null, null, EntityId.newId())))
                .isInstanceOf(PublicInvitationAlreadyExistsException.class);

        verify(invitationRepository, never()).save(any());
    }

    /** Un lien public qui désignerait un lot serait une invitation privée déguisée, avec le parcours d'un lien qui circule. */
    @Test
    void creating_a_public_invitation_that_designates_a_unit_is_refused() {
        EntityId propertyId = EntityId.newId();
        knownProperty(propertyId);

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(
                propertyId, InvitationType.PUBLIC, null, EntityId.newId(), EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void creating_a_private_invitation_records_the_contact_the_lot_and_emails_the_link() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        knownProperty(propertyId);
        knownUnit(propertyId, unitId);
        EntityId partyId = knownContact(propertyId);

        newService().create(new CreateInvitationCommand(propertyId, InvitationType.PRIVATE, partyId, unitId,
                EntityId.newId()));

        Invitation saved = captureSaved();
        assertThat(saved.getType()).isEqualTo(InvitationType.PRIVATE);
        assertThat(saved.getTargetUnitId()).isEqualTo(unitId);
        assertThat(saved.getTargetPartyId()).isEqualTo(partyId);
        // L'adresse est lue sur la fiche du contact, jamais reçue de l'appelant.
        assertThat(saved.getTargetEmail()).isEqualTo(TARGET_EMAIL);

        // « Envoyer une invitation » n'est pas « fabriquer un lien et se débrouiller » :
        // le lot est nommé dans le corps, pas seulement caché derrière le lien.
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(eq(TARGET_EMAIL), any(), body.capture());
        assertThat(body.getValue()).contains("A-12").contains("Copro Test")
                .contains("https://app.example.com/invitations?token=" + saved.getToken());
    }

    @Test
    void creating_an_invitation_for_an_unknown_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().create(
                new CreateInvitationCommand(propertyId, InvitationType.PUBLIC, null, null, EntityId.newId())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * Réinviter remplace l'invitation qui courait, au lieu d'en faire courir
     * deux : sans cela « une invitation est en cours » deviendrait ambigu dès
     * le premier renvoi, et deux liens vivants circuleraient pour un lot.
     */
    @Test
    void re_inviting_a_contact_closes_the_invitation_that_was_still_running() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        knownProperty(propertyId);
        knownUnit(propertyId, unitId);
        EntityId partyId = knownContact(propertyId);
        Invitation outstanding = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE,
                "PROPERTY_OWNER", TARGET_EMAIL, "old-tok", Instant.EPOCH.plus(Duration.ofDays(30)), EntityId.newId(),
                null, unitId, partyId);
        when(invitationRepository.findOutstandingPrivateByPartyId(partyId)).thenReturn(Optional.of(outstanding));

        newService().create(new CreateInvitationCommand(propertyId, InvitationType.PRIVATE, partyId, unitId,
                EntityId.newId()));

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getToken()).isEqualTo("old-tok");
        assertThat(captor.getAllValues().get(0).getStatus()).isEqualTo(InvitationStatus.DISABLED);
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(InvitationStatus.ACTIVE);
    }

    @Test
    void creating_a_private_invitation_without_a_contact_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        knownProperty(propertyId);
        knownUnit(propertyId, unitId);

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(
                propertyId, InvitationType.PRIVATE, null, unitId, EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void creating_a_private_invitation_without_a_lot_is_rejected() {
        EntityId propertyId = EntityId.newId();
        knownProperty(propertyId);

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(
                propertyId, InvitationType.PRIVATE, knownContact(propertyId), null, EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Il n'y a nulle part où envoyer le lien. */
    @Test
    void creating_a_private_invitation_for_a_contact_without_an_email_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        knownProperty(propertyId);
        knownUnit(propertyId, unitId);
        when(partyDirectoryPort.findById(partyId))
                .thenReturn(Optional.of(new PartyContactInfo(propertyId, "Jane Doe", null)));

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(
                propertyId, InvitationType.PRIVATE, partyId, unitId, EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(invitationRepository, never()).save(any());
    }

    /** Jamais de confiance dans un identifiant venu du client : un contact d'une autre copropriété ouvrirait un accès indu. */
    @Test
    void creating_a_private_invitation_for_a_contact_of_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        knownProperty(propertyId);
        knownUnit(propertyId, unitId);
        when(partyDirectoryPort.findById(partyId))
                .thenReturn(Optional.of(new PartyContactInfo(EntityId.newId(), "Jane Doe", TARGET_EMAIL)));

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(
                propertyId, InvitationType.PRIVATE, partyId, unitId, EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(invitationRepository, never()).save(any());
    }

    /** Idem pour le lot. */
    @Test
    void creating_a_private_invitation_for_a_lot_of_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        knownProperty(propertyId);
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(EntityId.newId(), "A-12", "Appartement", false)));

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(
                propertyId, InvitationType.PRIVATE, knownContact(propertyId), unitId, EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(invitationRepository, never()).save(any());
    }
}
