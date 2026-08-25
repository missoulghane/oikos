package com.architek.oikos.invitation.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.invitation.application.command.AcceptMembershipRequestCommand;
import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewStatus;
import com.architek.oikos.invitation.application.dto.MembershipRequestSortField;
import com.architek.oikos.invitation.application.command.RejectMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.AcceptMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.in.ListMembershipRequestsUseCase;
import com.architek.oikos.invitation.application.port.in.RejectMembershipRequestUseCase;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.invitation.web.request.RejectMembershipRequestRequest;
import com.architek.oikos.invitation.web.response.PagedMembershipRequestResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class MembershipRequestController {

    private final ListMembershipRequestsUseCase listMembershipRequestsUseCase;
    private final AcceptMembershipRequestUseCase acceptMembershipRequestUseCase;
    private final RejectMembershipRequestUseCase rejectMembershipRequestUseCase;

    public MembershipRequestController(ListMembershipRequestsUseCase listMembershipRequestsUseCase,
                                        AcceptMembershipRequestUseCase acceptMembershipRequestUseCase,
                                        RejectMembershipRequestUseCase rejectMembershipRequestUseCase) {
        this.listMembershipRequestsUseCase = listMembershipRequestsUseCase;
        this.acceptMembershipRequestUseCase = acceptMembershipRequestUseCase;
        this.rejectMembershipRequestUseCase = rejectMembershipRequestUseCase;
    }

    /**
     * Défauts choisis pour une file d'attente : la plus récente en premier,
     * tous statuts confondus. Le tableau du syndic s'ouvre donc sur ce qui
     * vient d'arriver, sans qu'il ait à toucher un filtre.
     */
    @PreAuthorize("@propertyAccess.canManageInvitations(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/membership-requests")
    public PagedMembershipRequestResponse list(@PathVariable String propertyId,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(required = false) MembershipRequestOverviewStatus status,
                                                @RequestParam(defaultValue = "SUBMITTED_AT") MembershipRequestSortField sortBy,
                                                @RequestParam(defaultValue = "DESC") SortDirection sortDirection,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return PagedMembershipRequestResponse.from(listMembershipRequestsUseCase.listMembershipRequests(
                new ListMembershipRequestsQuery(EntityId.of(propertyId), search, status, sortBy, sortDirection,
                        PageRequest.of(page, size))));
    }

    @PreAuthorize("@propertyAccess.canManageMembershipRequest(authentication, #id)")
    @PatchMapping("/membership-requests/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable String id, Authentication authentication) {
        acceptMembershipRequestUseCase.accept(
                new AcceptMembershipRequestCommand(MembershipRequestId.of(id), currentUserId(authentication)));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.canManageMembershipRequest(authentication, #id)")
    @PatchMapping("/membership-requests/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable String id,
                                        @RequestBody(required = false) RejectMembershipRequestRequest request,
                                        Authentication authentication) {
        rejectMembershipRequestUseCase.reject(new RejectMembershipRequestCommand(MembershipRequestId.of(id),
                currentUserId(authentication), request != null ? request.reason() : null));
        return ResponseEntity.noContent().build();
    }

    private EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
