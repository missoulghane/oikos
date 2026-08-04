package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.application.query.GetMembershipRequestQuery;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetMembershipRequestServiceTest {

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Test
    void returns_the_view_of_an_existing_request() {
        EntityId propertyId = EntityId.newId();
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(), propertyId,
                EntityId.newId(), EntityId.newId(), EntityId.newId());
        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        MembershipRequestView view = new GetMembershipRequestService(membershipRequestRepository)
                .getMembershipRequest(new GetMembershipRequestQuery(request.getId()));

        assertThat(view.id()).isEqualTo(request.getId());
        assertThat(view.propertyId()).isEqualTo(propertyId);
    }

    @Test
    void throws_when_the_request_does_not_exist() {
        MembershipRequestId id = MembershipRequestId.newId();
        when(membershipRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetMembershipRequestService(membershipRequestRepository)
                .getMembershipRequest(new GetMembershipRequestQuery(id)))
                .isInstanceOf(MembershipRequestNotFoundException.class);
    }
}
