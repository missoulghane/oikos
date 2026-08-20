package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

public record RegisterPropertyBoardAdminCommand(String fullName, EmailVO email, String phone,
                                                 RawPassword password, String propertyName,
                                                 String propertyAddress, String propertyCity) {
}
