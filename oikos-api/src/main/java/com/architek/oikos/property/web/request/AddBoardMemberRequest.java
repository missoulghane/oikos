package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.architek.oikos.property.domain.valueobject.BoardRole;

public record AddBoardMemberRequest(
        @NotBlank String partyId,
        @NotNull BoardRole boardRole) {
}
