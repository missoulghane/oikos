package com.architek.oikos.user.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.port.in.GetMyMembershipRequestsUseCase;
import com.architek.oikos.user.application.port.out.MembershipRequestDirectoryPort;
import com.architek.oikos.user.application.port.out.OwnedMembershipRequestView;
import com.architek.oikos.user.application.query.GetMyMembershipRequestsQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.repository.UserRepository;

/**
 * Unlike GetMyUnitsService/GetMyInstallmentsService, this does not fan out
 * over the user's linked party ids: a membership request's party is only
 * linked to the account once a manager accepts it (see
 * GrantPropertyRoleService), so looking it up by partyId would miss every
 * still-PENDING request. MembershipRequestDirectoryPort is keyed by userId
 * directly instead - see its Javadoc.
 */
@Component
public class GetMyMembershipRequestsService implements GetMyMembershipRequestsUseCase {

    private final UserRepository userRepository;
    private final MembershipRequestDirectoryPort membershipRequestDirectoryPort;

    public GetMyMembershipRequestsService(UserRepository userRepository,
                                           MembershipRequestDirectoryPort membershipRequestDirectoryPort) {
        this.userRepository = userRepository;
        this.membershipRequestDirectoryPort = membershipRequestDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnedMembershipRequestView> getMyMembershipRequests(GetMyMembershipRequestsQuery query) {
        userRepository.findById(query.userId()).orElseThrow(() -> new UserNotFoundException(query.userId()));
        return membershipRequestDirectoryPort.listMembershipRequestsForUser(query.userId().value());
    }
}
