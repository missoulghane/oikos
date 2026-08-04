package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.port.out.AvailableUnitInfo;
import com.architek.oikos.invitation.application.query.ListAvailableUnitsForInvitationQuery;
import com.architek.oikos.shared.domain.pagination.Page;

/**
 * Public: only meaningful for PUBLIC/PRIVATE_WITHOUT_UNIT invitations (a
 * PRIVATE_WITH_UNIT invitation's unit is already fixed).
 */
public interface ListAvailableUnitsForInvitationUseCase {

    Page<AvailableUnitInfo> list(ListAvailableUnitsForInvitationQuery query);
}
