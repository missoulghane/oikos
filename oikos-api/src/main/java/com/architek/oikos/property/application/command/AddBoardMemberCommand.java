package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AddBoardMemberCommand(PropertyId propertyId, EntityId contactId, BoardRole boardRole) {
}
