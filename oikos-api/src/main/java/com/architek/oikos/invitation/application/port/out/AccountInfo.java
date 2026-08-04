package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record AccountInfo(EmailVO email, String fullName) {
}
