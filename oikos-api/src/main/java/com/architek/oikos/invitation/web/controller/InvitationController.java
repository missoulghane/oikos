package com.architek.oikos.invitation.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.invitation.application.command.CreateInvitationCommand;
import com.architek.oikos.invitation.application.command.DisableInvitationCommand;
import com.architek.oikos.invitation.application.port.in.CreateInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.DisableInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.GetInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.ListInvitationsUseCase;
import com.architek.oikos.invitation.application.query.GetInvitationQuery;
import com.architek.oikos.invitation.application.query.ListInvitationsQuery;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.invitation.web.request.CreateInvitationRequest;
import com.architek.oikos.invitation.web.response.InvitationResponse;
import com.architek.oikos.invitation.web.response.PagedInvitationResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class InvitationController {

    private final CreateInvitationUseCase createInvitationUseCase;
    private final ListInvitationsUseCase listInvitationsUseCase;
    private final GetInvitationUseCase getInvitationUseCase;
    private final DisableInvitationUseCase disableInvitationUseCase;

    public InvitationController(CreateInvitationUseCase createInvitationUseCase, ListInvitationsUseCase listInvitationsUseCase,
                                 GetInvitationUseCase getInvitationUseCase, DisableInvitationUseCase disableInvitationUseCase) {
        this.createInvitationUseCase = createInvitationUseCase;
        this.listInvitationsUseCase = listInvitationsUseCase;
        this.getInvitationUseCase = getInvitationUseCase;
        this.disableInvitationUseCase = disableInvitationUseCase;
    }

    @PreAuthorize("@propertyAccess.canManageInvitations(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/invitations")
    public ResponseEntity<Void> create(@PathVariable String propertyId, @Valid @RequestBody CreateInvitationRequest request,
                                        Authentication authentication) {
        InvitationId id = createInvitationUseCase.create(new CreateInvitationCommand(
                EntityId.of(propertyId), request.type(),
                request.unitId() != null ? EntityId.of(request.unitId()) : null,
                request.targetEmail() != null ? EmailVO.of(request.targetEmail()) : null,
                currentUserId(authentication)));
        return ResponseEntity.created(URI.create("/api/v1/invitations/" + id)).build();
    }

    @PreAuthorize("@propertyAccess.canManageInvitations(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/invitations")
    public PagedInvitationResponse list(@PathVariable String propertyId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return PagedInvitationResponse.from(listInvitationsUseCase.listInvitations(
                new ListInvitationsQuery(EntityId.of(propertyId), PageRequest.of(page, size))));
    }

    @PreAuthorize("@propertyAccess.canManageInvitation(authentication, #id)")
    @GetMapping("/invitations/{id}")
    public InvitationResponse getById(@PathVariable String id) {
        return InvitationResponse.from(getInvitationUseCase.getInvitation(new GetInvitationQuery(InvitationId.of(id))));
    }

    @PreAuthorize("@propertyAccess.canManageInvitation(authentication, #id)")
    @PatchMapping("/invitations/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable String id) {
        disableInvitationUseCase.disable(new DisableInvitationCommand(InvitationId.of(id)));
        return ResponseEntity.noContent().build();
    }

    private EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
