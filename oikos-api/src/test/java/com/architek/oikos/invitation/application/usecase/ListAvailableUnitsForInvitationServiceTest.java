package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.port.out.AvailableUnitInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.query.ListAvailableUnitsForInvitationQuery;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListAvailableUnitsForInvitationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    private ListAvailableUnitsForInvitationService newService() {
        return new ListAvailableUnitsForInvitationService(invitationRepository, unitDirectoryPort, CLOCK);
    }

    @Test
    void listing_for_a_private_invitation_delegates_to_the_unit_directory_port() {
        EntityId propertyId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE,
                "PROPERTY_OWNER", EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)),
                EntityId.newId(), null);
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(unitDirectoryPort.listAvailable(propertyId, PageRequest.of(0, 20)))
                .thenReturn(Page.of(List.of(new AvailableUnitInfo(EntityId.newId(), "A1", "Appartement")), 0, 20, 1));

        Page<AvailableUnitInfo> result = newService().list(new ListAvailableUnitsForInvitationQuery("tok", PageRequest.of(0, 20)));

        assertThat(result.content()).hasSize(1);
    }
}
