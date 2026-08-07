package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.port.out.MembershipRequestDirectoryPort;
import com.architek.oikos.user.application.port.out.OwnedMembershipRequestView;
import com.architek.oikos.user.application.query.GetMyMembershipRequestsQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class GetMyMembershipRequestsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipRequestDirectoryPort membershipRequestDirectoryPort;

    private GetMyMembershipRequestsService newService() {
        return new GetMyMembershipRequestsService(userRepository, membershipRequestDirectoryPort);
    }

    @Test
    void returns_the_requests_submitted_by_this_user_including_still_pending_ones() {
        UserId userId = UserId.newId();
        User user = User.register(userId, EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        OwnedMembershipRequestView pendingRequest = new OwnedMembershipRequestView(EntityId.newId(), "Copro Test", "A1",
                "Appartement", "PENDING", null, null);
        when(membershipRequestDirectoryPort.listMembershipRequestsForUser(userId.value())).thenReturn(List.of(pendingRequest));

        List<OwnedMembershipRequestView> result = newService().getMyMembershipRequests(new GetMyMembershipRequestsQuery(userId));

        assertThat(result).containsExactly(pendingRequest);
    }

    @Test
    void unknown_user_is_rejected() {
        UserId userId = UserId.newId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getMyMembershipRequests(new GetMyMembershipRequestsQuery(userId)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
