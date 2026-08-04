package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.command.RejectMembershipRequestCommand;
import com.architek.oikos.invitation.domain.exception.MembershipRequestAlreadyDecidedException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RejectMembershipRequestServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    private RejectMembershipRequestService newService() {
        return new RejectMembershipRequestService(membershipRequestRepository, CLOCK);
    }

    @Test
    void rejecting_a_pending_request_saves_it_with_the_reason() {
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(),
                EntityId.newId(), EntityId.newId(), EntityId.newId(), EntityId.newId());
        EntityId decidedByUserId = EntityId.newId();
        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(membershipRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().reject(new RejectMembershipRequestCommand(request.getId(), decidedByUserId, "Lot déjà attribué"));

        ArgumentCaptor<MembershipRequest> captor = ArgumentCaptor.forClass(MembershipRequest.class);
        verify(membershipRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus().name()).isEqualTo("REJECTED");
        assertThat(captor.getValue().getRejectionReason()).isEqualTo("Lot déjà attribué");
        assertThat(captor.getValue().getDecidedByUserId()).isEqualTo(decidedByUserId);
    }

    @Test
    void rejecting_an_unknown_request_throws() {
        MembershipRequestId id = MembershipRequestId.newId();
        when(membershipRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().reject(new RejectMembershipRequestCommand(id, EntityId.newId(), null)))
                .isInstanceOf(MembershipRequestNotFoundException.class);
    }

    @Test
    void rejecting_an_already_decided_request_throws() {
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(),
                EntityId.newId(), EntityId.newId(), EntityId.newId(), EntityId.newId())
                .reject(CLOCK.instant(), EntityId.newId(), "already rejected");
        when(membershipRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> newService().reject(new RejectMembershipRequestCommand(request.getId(), EntityId.newId(), null)))
                .isInstanceOf(MembershipRequestAlreadyDecidedException.class);
    }
}
