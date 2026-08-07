package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewStatus;
import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListMembershipRequestsServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Mock
    private InvitationRepository invitationRepository;

    private ListMembershipRequestsService newService() {
        return new ListMembershipRequestsService(membershipRequestRepository, invitationRepository, CLOCK);
    }

    @Test
    void a_real_membership_request_is_surfaced_with_its_own_status() {
        EntityId propertyId = EntityId.newId();
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(), propertyId,
                EntityId.newId(), EntityId.newId(), EntityId.newId());
        when(membershipRequestRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(request));
        when(invitationRepository.findAllByPropertyIdAndTypeAndStatus(propertyId, InvitationType.PRIVATE, InvitationStatus.ACTIVE))
                .thenReturn(List.of());

        Page<MembershipRequestOverviewView> result = newService()
                .listMembershipRequests(new ListMembershipRequestsQuery(propertyId, PageRequest.of(0, 20)));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status()).isEqualTo(MembershipRequestOverviewStatus.PENDING);
        assertThat(result.content().get(0).id()).isEqualTo(EntityId.of(request.getId().asUuid()));
    }

    @Test
    void a_usable_active_private_invitation_with_no_matching_request_is_surfaced_as_invited() {
        EntityId propertyId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE, "PROPERTY_OWNER",
                EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId(), null);
        when(membershipRequestRepository.findAllByPropertyId(propertyId)).thenReturn(List.of());
        when(invitationRepository.findAllByPropertyIdAndTypeAndStatus(propertyId, InvitationType.PRIVATE, InvitationStatus.ACTIVE))
                .thenReturn(List.of(invitation));

        Page<MembershipRequestOverviewView> result = newService()
                .listMembershipRequests(new ListMembershipRequestsQuery(propertyId, PageRequest.of(0, 20)));

        assertThat(result.content()).hasSize(1);
        MembershipRequestOverviewView view = result.content().get(0);
        assertThat(view.status()).isEqualTo(MembershipRequestOverviewStatus.INVITED);
        assertThat(view.targetEmail()).isEqualTo(EmailVO.of("jane.doe@example.com"));
        assertThat(view.unitId()).isNull();
        assertThat(view.partyId()).isNull();
    }

    @Test
    void an_expired_but_still_active_status_private_invitation_is_excluded() {
        EntityId propertyId = EntityId.newId();
        Invitation expired = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE, "PROPERTY_OWNER",
                EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().minus(Duration.ofDays(1)), EntityId.newId(), null);
        when(membershipRequestRepository.findAllByPropertyId(propertyId)).thenReturn(List.of());
        when(invitationRepository.findAllByPropertyIdAndTypeAndStatus(propertyId, InvitationType.PRIVATE, InvitationStatus.ACTIVE))
                .thenReturn(List.of(expired));

        Page<MembershipRequestOverviewView> result = newService()
                .listMembershipRequests(new ListMembershipRequestsQuery(propertyId, PageRequest.of(0, 20)));

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void invited_entries_are_listed_before_real_requests_and_pagination_applies_to_the_merged_list() {
        EntityId propertyId = EntityId.newId();
        Invitation invitation = Invitation.issue(InvitationId.newId(), propertyId, InvitationType.PRIVATE, "PROPERTY_OWNER",
                EmailVO.of("jane.doe@example.com"), "tok", CLOCK.instant().plus(Duration.ofDays(1)), EntityId.newId(), null);
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(), propertyId,
                EntityId.newId(), EntityId.newId(), EntityId.newId());
        when(membershipRequestRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(request));
        when(invitationRepository.findAllByPropertyIdAndTypeAndStatus(propertyId, InvitationType.PRIVATE, InvitationStatus.ACTIVE))
                .thenReturn(List.of(invitation));

        Page<MembershipRequestOverviewView> firstPage = newService()
                .listMembershipRequests(new ListMembershipRequestsQuery(propertyId, PageRequest.of(0, 1)));
        Page<MembershipRequestOverviewView> secondPage = newService()
                .listMembershipRequests(new ListMembershipRequestsQuery(propertyId, PageRequest.of(1, 1)));

        assertThat(firstPage.totalElements()).isEqualTo(2);
        assertThat(firstPage.content()).hasSize(1);
        assertThat(firstPage.content().get(0).status()).isEqualTo(MembershipRequestOverviewStatus.INVITED);
        assertThat(secondPage.content()).hasSize(1);
        assertThat(secondPage.content().get(0).status()).isEqualTo(MembershipRequestOverviewStatus.PENDING);
    }
}
