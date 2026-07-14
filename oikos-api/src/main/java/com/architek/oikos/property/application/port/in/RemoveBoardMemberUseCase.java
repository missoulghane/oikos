package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.RemoveBoardMemberCommand;

public interface RemoveBoardMemberUseCase {

    void remove(RemoveBoardMemberCommand command);
}
