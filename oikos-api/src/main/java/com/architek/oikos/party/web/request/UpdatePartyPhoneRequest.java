package com.architek.oikos.party.web.request;

import jakarta.validation.constraints.Size;

public record UpdatePartyPhoneRequest(@Size(max = 20) String phone) {
}
