package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;

public interface AddBoardMemberUseCase {

    BoardMemberId add(AddBoardMemberCommand command);
}
