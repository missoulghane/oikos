package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.CreatePendingBoardMemberCommand;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;

public interface CreatePendingBoardMemberUseCase {

    BoardMemberId create(CreatePendingBoardMemberCommand command);
}
