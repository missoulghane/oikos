package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.query.ListInvitationsQuery;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListInvitationsServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private InvitationLinkComposer linkComposer;

    private ListInvitationsService newService() {
        return new ListInvitationsService(invitationRepository, linkComposer);
    }

    @Test
    void listing_invitations_of_a_property_maps_each_to_a_view_with_a_link() {
        EntityId propertyId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PUBLIC, "PROPERTY_OWNER",
                null, "tok", Instant.EPOCH.plus(Duration.ofDays(30)), EntityId.newId(), null);
        when(invitationRepository.findAllByPropertyId(propertyId, PageRequest.of(0, 20)))
                .thenReturn(Page.of(List.of(invitation), 0, 20, 1));
        when(linkComposer.link("tok")).thenReturn("http://localhost/invitations?token=tok");

        Page<InvitationView> page = newService().listInvitations(new ListInvitationsQuery(propertyId, PageRequest.of(0, 20)));

        assertThat(page.content()).extracting(InvitationView::link).containsExactly("http://localhost/invitations?token=tok");
    }
}
