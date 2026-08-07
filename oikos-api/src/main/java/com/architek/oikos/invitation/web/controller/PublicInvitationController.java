package com.architek.oikos.invitation.web.controller;

import org.springframework.http.ResponseEntity;
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
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Preview and available-units stay anonymous (see SecurityConfiguration's
 * GET-only permitAll entry for "/api/v1/invitations/by-token/**") so the
 * landing page can render before the visitor logs in. accept/membership-
 * requests, on the other hand, require an authenticated caller - enforced by
 * SecurityConfiguration's URL matcher (falls through to
 * .anyRequest().authenticated()) rather than a @PreAuthorize here, so a
 * missing/expired token is rejected before reaching this controller (401,
 * same as every other secured endpoint) instead of surfacing as a 403 that
 * the frontend's token-refresh interceptor doesn't retry: account creation
 * now always happens upstream, through the standard registration + email-
 * verification flow, before the invitation wizard's second step ever calls
 * these two endpoints.
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
        EntityId unitId = request != null && request.unitId() != null ? EntityId.of(request.unitId()) : null;
        acceptInvitationUseCase.accept(new AcceptInvitationCommand(token, actingUserId(authentication), unitId));
        return ResponseEntity.noContent().build();
    }

    /**
     * PUBLIC-type counterpart of accept: creates a PENDING MembershipRequest
     * instead of granting access immediately - a manager/board admin must
     * accept it first (see MembershipRequestController).
     */
    @PostMapping("/{token}/membership-requests")
    public ResponseEntity<Void> submitMembershipRequest(@PathVariable String token,
                                                          @RequestBody(required = false) AcceptInvitationRequest request,
                                                          Authentication authentication) {
        EntityId unitId = request != null && request.unitId() != null ? EntityId.of(request.unitId()) : null;
        submitMembershipRequestUseCase.submit(new SubmitMembershipRequestCommand(token, actingUserId(authentication), unitId));
        return ResponseEntity.noContent().build();
    }

    private EntityId actingUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
