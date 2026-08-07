package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * partyId identifies an existing party; when null, fullName/email/phone
 * describe a new party to be created inline instead (AddBoardMemberService
 * enforces this XOR - not expressible declaratively on the web request, same
 * convention as CreateInvitationRequest).
 */
public record AddBoardMemberCommand(PropertyId propertyId, EntityId partyId, String fullName, String email,
                                     String phone, BoardRole boardRole) {
}
