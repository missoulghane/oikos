package com.architek.oikos.invitation.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.AcceptInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.GetInvitationByTokenUseCase;
import com.architek.oikos.invitation.application.port.in.ListAvailableUnitsForInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.SubmitMembershipRequestUseCase;
import com.architek.oikos.invitation.application.query.GetInvitationByTokenQuery;
import com.architek.oikos.invitation.application.query.ListAvailableUnitsForInvitationQuery;
import com.architek.oikos.invitation.web.request.AcceptInvitationRequest;
import com.architek.oikos.invitation.web.response.InvitationPreviewResponse;
import com.architek.oikos.invitation.web.response.PagedAvailableUnitResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Public/anonymous invitation consumption. Every endpoint here is
 * permitAll'd (see SecurityConfiguration's "/api/v1/invitations/by-token/**"
 * entry), but accept/candidacies still branch on Authentication: if the
 * request carries a valid JWT (JwtAuthenticationFilter runs for every
 * request regardless of permitAll status), it's treated as "log in and
 * resume" - the caller's own account/party is reused instead of provisioning
 * a new one. This is the whole mechanism behind the invitation landing
 * page's inline "I already have an account" flow: no separate authenticated
 * endpoint, no returnTo/redirect plumbing needed.
 */
@RestController
@RequestMapping("/invitations/by-token")
public class PublicInvitationController {

    private final GetInvitationByTokenUseCase getInvitationByTokenUseCase;
    private final ListAvailableUnitsForInvitationUseCase listAvailableUnitsForInvitationUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final SubmitMembershipRequestUseCase submitMembershipRequestUseCase;

    public PublicInvitationController(GetInvitationByTokenUseCase getInvitationByTokenUseCase,
                                       ListAvailableUnitsForInvitationUseCase listAvailableUnitsForInvitationUseCase,
                                       AcceptInvitationUseCase acceptInvitationUseCase,
                                       SubmitMembershipRequestUseCase submitMembershipRequestUseCase) {
        this.getInvitationByTokenUseCase = getInvitationByTokenUseCase;
        this.listAvailableUnitsForInvitationUseCase = listAvailableUnitsForInvitationUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.submitMembershipRequestUseCase = submitMembershipRequestUseCase;
    }

    @GetMapping("/{token}")
    public InvitationPreviewResponse preview(@PathVariable String token) {
        return InvitationPreviewResponse.from(getInvitationByTokenUseCase.getPreview(new GetInvitationByTokenQuery(token)));
    }

    @GetMapping("/{token}/available-units")
    public PagedAvailableUnitResponse availableUnits(@PathVariable String token,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return PagedAvailableUnitResponse.from(listAvailableUnitsForInvitationUseCase.list(
                new ListAvailableUnitsForInvitationQuery(token, PageRequest.of(page, size))));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<Void> accept(@PathVariable String token,
                                        @RequestBody(required = false) AcceptInvitationRequest request,
                                        Authentication authentication) {
        EntityId actingUserId = actingUserId(authentication);
        EntityId unitId = request != null && request.unitId() != null ? EntityId.of(request.unitId()) : null;
        AcceptInvitationCommand command = actingUserId != null
                ? new AcceptInvitationCommand(token, actingUserId, null, null, null, unitId)
                : new AcceptInvitationCommand(token, null,
                        request != null && request.email() != null ? EmailVO.of(request.email()) : null,
                        request != null ? request.fullName() : null,
                        request != null && request.password() != null ? RawPassword.of(request.password()) : null,
                        unitId);
        acceptInvitationUseCase.accept(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUBLIC-type counterpart of accept: creates a PENDING MembershipRequest
     * instead of granting access immediately - a manager/board admin must
     * accept it first (see MembershipRequestController). Reuses
     * AcceptInvitationRequest's shape (email/fullName/password/unitId) since
     * the frontend form is identical either way.
     */
    @PostMapping("/{token}/candidacies")
    public ResponseEntity<Void> submitCandidacy(@PathVariable String token,
                                                 @RequestBody(required = false) AcceptInvitationRequest request,
                                                 Authentication authentication) {
        EntityId actingUserId = actingUserId(authentication);
        EntityId unitId = request != null && request.unitId() != null ? EntityId.of(request.unitId()) : null;
        SubmitMembershipRequestCommand command = actingUserId != null
                ? new SubmitMembershipRequestCommand(token, actingUserId, null, null, null, unitId)
                : new SubmitMembershipRequestCommand(token, null,
                        request != null && request.email() != null ? EmailVO.of(request.email()) : null,
                        request != null ? request.fullName() : null,
                        request != null && request.password() != null ? RawPassword.of(request.password()) : null,
                        unitId);
        submitMembershipRequestUseCase.submit(command);
        return ResponseEntity.noContent().build();
    }

    private EntityId actingUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
