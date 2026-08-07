package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.ValidateBoardMemberCommand;

public interface ValidateBoardMemberUseCase {

    void validate(ValidateBoardMemberCommand command);
}
