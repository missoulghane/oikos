package com.architek.oikos.user.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.dto.MembershipRequestSummaryView;
import com.architek.oikos.invitation.application.port.in.ListMembershipRequestsByUserUseCase;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsByUserQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.MembershipRequestDirectoryPort;
import com.architek.oikos.user.application.port.out.OwnedMembershipRequestView;

@Component
public class MembershipRequestDirectoryAdapter implements MembershipRequestDirectoryPort {

    private final ListMembershipRequestsByUserUseCase listMembershipRequestsByUserUseCase;

    public MembershipRequestDirectoryAdapter(ListMembershipRequestsByUserUseCase listMembershipRequestsByUserUseCase) {
        this.listMembershipRequestsByUserUseCase = listMembershipRequestsByUserUseCase;
    }

    @Override
    public List<OwnedMembershipRequestView> listMembershipRequestsForUser(EntityId userId) {
        return listMembershipRequestsByUserUseCase.listMembershipRequests(new ListMembershipRequestsByUserQuery(userId)).stream()
                .map(MembershipRequestDirectoryAdapter::toOwnedView)
                .toList();
    }

    private static OwnedMembershipRequestView toOwnedView(MembershipRequestSummaryView view) {
        return new OwnedMembershipRequestView(EntityId.of(view.id().asUuid()), view.propertyName(), view.unitNumber(),
                view.unitTypeName(), view.status().name(), view.decidedAt(), view.rejectionReason());
    }
}
