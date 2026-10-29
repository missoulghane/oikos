package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.command.CreateBoardInvitationCommand;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.service.InvitationTokenGenerator;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;
import com.architek.oikos.testsupport.TestEmailTemplates;

@ExtendWith(MockitoExtension.class)
class CreateBoardInvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    private CreateBoardInvitationService newService() {
        return new CreateBoardInvitationService(invitationRepository, propertyDirectoryPort,
                new InvitationTokenGenerator(), new InvitationLinkComposer("https://app.example.com/invitations"),
                new BoardInvitationEmailComposer(TestEmailTemplates.renderer()),
                code -> "PRESIDENT".equals(code) ? "Président" : code, emailSenderPort,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 30L);
    }

    @Test
    void creating_a_board_invitation_for_a_known_property_persists_it_as_private_with_the_chosen_role() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateBoardInvitationCommand(propertyId, EmailVO.of("jane.doe@example.com"),
                "PRESIDENT", EntityId.newId()));

        org.mockito.ArgumentCaptor<Invitation> captor = org.mockito.ArgumentCaptor.forClass(Invitation.class);
        org.mockito.Mockito.verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(InvitationType.PRIVATE);
        assertThat(captor.getValue().getTargetRole()).isEqualTo("PROPERTY_BOARD_MEMBER");
        assertThat(captor.getValue().getTargetBoardRole()).isEqualTo("PRESIDENT");
        assertThat(captor.getValue().getTargetEmail()).isEqualTo(EmailVO.of("jane.doe@example.com"));
    }

    @Test
    void the_invited_person_is_mailed_the_link_with_the_seat_named_as_on_screen() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateBoardInvitationCommand(propertyId, EmailVO.of("jane.doe@example.com"),
                "PRESIDENT", EntityId.newId()));

        org.mockito.ArgumentCaptor<Invitation> saved = org.mockito.ArgumentCaptor.forClass(Invitation.class);
        org.mockito.Mockito.verify(invitationRepository).save(saved.capture());
        org.mockito.ArgumentCaptor<String> body = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(emailSenderPort).send(org.mockito.ArgumentMatchers.eq(EmailVO.of("jane.doe@example.com")),
                org.mockito.ArgumentMatchers.eq("Daba Syndic - Invitation au conseil syndical de Copro Test"), body.capture());
        assertThat(body.getValue()).contains("Copro Test").contains("Président")
                .contains("https://app.example.com/invitations?token=" + saved.getValue().getToken())
                .contains("<span>30</span> jours");
    }

    @Test
    void creating_a_board_invitation_for_an_unknown_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().create(new CreateBoardInvitationCommand(
                propertyId, EmailVO.of("jane.doe@example.com"), "PRESIDENT", EntityId.newId())))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
