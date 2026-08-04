package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record ProvisionInvitedAccountCommand(EmailVO email, String fullName, RawPassword password) {
}
