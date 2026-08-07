package com.architek.oikos.invitation.application.usecase;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.MembershipRequestSummaryView;
import com.architek.oikos.invitation.application.port.in.ListMembershipRequestsByUserUseCase;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsByUserQuery;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;

/**
 * Enriches the requester's own MembershipRequest rows with the property/unit
 * names for display, the same way GetInvitationByTokenService enriches the
 * public preview - resolved here rather than left to the caller, consistent
 * with how the rest of this module treats id-resolution as this module's job.
 */
@Component
public class ListMembershipRequestsByUserService implements ListMembershipRequestsByUserUseCase {

    private final MembershipRequestRepository membershipRequestRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;

    public ListMembershipRequestsByUserService(MembershipRequestRepository membershipRequestRepository,
                                                PropertyDirectoryPort propertyDirectoryPort, UnitDirectoryPort unitDirectoryPort) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipRequestSummaryView> listMembershipRequests(ListMembershipRequestsByUserQuery query) {
        return membershipRequestRepository.findAllByUserId(query.userId()).stream()
                .map(this::toSummary)
                .toList();
    }

    private MembershipRequestSummaryView toSummary(MembershipRequest request) {
        String propertyName = propertyDirectoryPort.findBasicInfo(request.getPropertyId())
                .map(PropertyBasicInfo::name)
                .orElse(null);
        Optional<UnitBasicInfo> unit = unitDirectoryPort.findBasicInfo(request.getUnitId());
        return new MembershipRequestSummaryView(request.getId(), propertyName,
                unit.map(UnitBasicInfo::unitNumber).orElse(null), unit.map(UnitBasicInfo::unitTypeName).orElse(null),
                request.getStatus(), request.getDecidedAt(), request.getRejectionReason());
    }
}
