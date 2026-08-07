package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotNull;
import com.architek.oikos.property.domain.valueobject.BoardRole;

/**
 * partyId is optional: when absent, fullName (required)/email/phone describe
 * a new party to create inline instead. This XOR isn't expressible
 * declaratively in bean validation (same convention as
 * CreateInvitationRequest), so it's enforced in AddBoardMemberService.
 */
public record AddBoardMemberRequest(
        String partyId,
        String fullName,
        String email,
        String phone,
        @NotNull BoardRole boardRole) {
}
