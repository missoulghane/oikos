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

import com.architek.oikos.invitation.application.command.CreateInvitationCommand;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.service.InvitationTokenGenerator;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CreateInvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    private CreateInvitationService newService() {
        return new CreateInvitationService(invitationRepository, propertyDirectoryPort, unitDirectoryPort,
                new InvitationTokenGenerator(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 30L);
    }

    @Test
    void creating_a_public_invitation_for_a_known_property_persists_it() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));
        when(invitationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateInvitationCommand(propertyId, InvitationType.PUBLIC, null, null, EntityId.newId()));

        org.mockito.ArgumentCaptor<Invitation> captor = org.mockito.ArgumentCaptor.forClass(Invitation.class);
        org.mockito.Mockito.verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(InvitationType.PUBLIC);
        assertThat(captor.getValue().getTargetRole()).isEqualTo("PROPERTY_OWNER");
    }

    @Test
    void creating_an_invitation_for_an_unknown_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().create(
                new CreateInvitationCommand(propertyId, InvitationType.PUBLIC, null, null, EntityId.newId())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creating_a_private_invitation_without_a_target_email_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(
                propertyId, InvitationType.PRIVATE_WITHOUT_UNIT, null, null, EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creating_a_with_unit_invitation_without_a_unit_id_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(propertyId, InvitationType.PRIVATE_WITH_UNIT,
                null, EmailVO.of("jane.doe@example.com"), EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creating_a_with_unit_invitation_for_an_already_owned_unit_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", false)));

        assertThatThrownBy(() -> newService().create(new CreateInvitationCommand(propertyId, InvitationType.PRIVATE_WITH_UNIT,
                unitId, EmailVO.of("jane.doe@example.com"), EntityId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
