package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;
import com.architek.oikos.invitation.application.port.in.ListMembershipRequestsUseCase;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

/**
 * Merges real, persisted MembershipRequest rows with still-outstanding
 * PRIVATE invitations (issued, not yet accepted - see
 * MembershipRequestOverviewView) into one unified, manager-facing overview.
 * Both sources are fetched unpaged and paginated in memory here, since two
 * independently-paged JPA queries can't be merged into a single consistent
 * page. Expected volume per property is modest (tens, not thousands) - worth
 * revisiting only if that stops holding.
 */
@Component
public class ListMembershipRequestsService implements ListMembershipRequestsUseCase {

    private final MembershipRequestRepository membershipRequestRepository;
    private final InvitationRepository invitationRepository;
    private final Clock clock;

    public ListMembershipRequestsService(MembershipRequestRepository membershipRequestRepository,
                                          InvitationRepository invitationRepository, Clock clock) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.invitationRepository = invitationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MembershipRequestOverviewView> listMembershipRequests(ListMembershipRequestsQuery query) {
        List<MembershipRequestOverviewView> invited = invitationRepository
                .findAllByPropertyIdAndTypeAndStatus(query.propertyId(), InvitationType.PRIVATE, InvitationStatus.ACTIVE)
                .stream()
                .filter(invitation -> invitation.isUsable(clock.instant()))
                .map(MembershipRequestOverviewView::fromInvitation)
                .toList();
        List<MembershipRequestOverviewView> requests = membershipRequestRepository.findAllByPropertyId(query.propertyId())
                .stream()
                .map(MembershipRequestOverviewView::fromRequest)
                .toList();

        List<MembershipRequestOverviewView> merged = new ArrayList<>(invited.size() + requests.size());
        merged.addAll(invited);
        merged.addAll(requests);

        return paginate(merged, query.pageRequest());
    }

    private static Page<MembershipRequestOverviewView> paginate(List<MembershipRequestOverviewView> merged, PageRequest pageRequest) {
        int fromIndex = Math.min(pageRequest.pageNumber() * pageRequest.pageSize(), merged.size());
        int toIndex = Math.min(fromIndex + pageRequest.pageSize(), merged.size());
        return Page.of(merged.subList(fromIndex, toIndex), pageRequest.pageNumber(), pageRequest.pageSize(), merged.size());
    }
}
