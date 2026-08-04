package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListMembershipRequestsServiceTest {

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Test
    void lists_membership_requests_for_a_property_as_views() {
        EntityId propertyId = EntityId.newId();
        PageRequest pageRequest = PageRequest.of(0, 20);
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(), propertyId,
                EntityId.newId(), EntityId.newId(), EntityId.newId());
        when(membershipRequestRepository.findAllByPropertyId(propertyId, pageRequest))
                .thenReturn(Page.of(java.util.List.of(request), 0, 20, 1));

        Page<MembershipRequestView> result = new ListMembershipRequestsService(membershipRequestRepository)
                .listMembershipRequests(new ListMembershipRequestsQuery(propertyId, pageRequest));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo(request.getId());
        assertThat(result.content().get(0).propertyId()).isEqualTo(propertyId);
    }
}
