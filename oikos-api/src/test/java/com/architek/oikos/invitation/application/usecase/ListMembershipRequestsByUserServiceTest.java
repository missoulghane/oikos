package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.invitation.application.dto.MembershipRequestSummaryView;
import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsByUserQuery;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListMembershipRequestsByUserServiceTest {

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    private ListMembershipRequestsByUserService newService() {
        return new ListMembershipRequestsByUserService(membershipRequestRepository, propertyDirectoryPort, unitDirectoryPort);
    }

    @Test
    void lists_requests_for_a_user_enriched_with_property_and_unit_names_even_when_still_pending() {
        EntityId userId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(), EntityId.newId(), propertyId,
                unitId, EntityId.newId(), userId);
        when(membershipRequestRepository.findAllByUserId(userId)).thenReturn(List.of(request));
        when(propertyDirectoryPort.findBasicInfo(propertyId))
                .thenReturn(Optional.of(new PropertyBasicInfo("Copro Test", "1 rue de la Paix")));
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(propertyId, "A1", "Appartement", false)));

        List<MembershipRequestSummaryView> result = newService()
                .listMembershipRequests(new ListMembershipRequestsByUserQuery(userId));

        assertThat(result).hasSize(1);
        MembershipRequestSummaryView summary = result.get(0);
        assertThat(summary.propertyName()).isEqualTo("Copro Test");
        assertThat(summary.unitNumber()).isEqualTo("A1");
        assertThat(summary.unitTypeName()).isEqualTo("Appartement");
        assertThat(summary.status()).isEqualTo(request.getStatus());
    }
}
