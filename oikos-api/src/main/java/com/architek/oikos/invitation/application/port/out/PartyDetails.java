package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record PartyDetails(String fullName, PartyType partyType, EmailVO email, String phone) {
}
