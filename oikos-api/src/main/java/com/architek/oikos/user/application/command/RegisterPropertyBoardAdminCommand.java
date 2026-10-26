package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Step 1 of the volunteer-syndic wizard: the account only. Acceptance of the
 * terms is enforced at the web boundary (a request without it never becomes a
 * command), so reaching the use case means they were accepted.
 */
public record RegisterPropertyBoardAdminCommand(String fullName, EmailVO email, String phone,
                                                 RawPassword password) {
}
