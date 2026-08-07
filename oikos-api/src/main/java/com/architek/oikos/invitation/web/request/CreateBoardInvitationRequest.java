package com.architek.oikos.invitation.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import com.architek.oikos.property.domain.valueobject.BoardRole;

/**
 * References property's BoardRole directly from the web layer - same
 * accepted precedent as PropertyController (see Invitation's javadoc) rather
 * than invitation's usual cross-module decoupling, since a web request DTO
 * choosing among a fixed enum of roles is display/input concern, not domain
 * coupling.
 */
public record CreateBoardInvitationRequest(@NotNull @Email String targetEmail, @NotNull BoardRole boardRole) {
}
