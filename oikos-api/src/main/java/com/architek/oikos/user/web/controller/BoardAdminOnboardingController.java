package com.architek.oikos.user.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.auth.application.port.out.JwtTokenPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.CreateBoardAdminPropertyCommand;
import com.architek.oikos.user.application.port.in.CreateBoardAdminPropertyUseCase;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.web.request.CreateBoardAdminPropertyRequest;
import com.architek.oikos.user.web.response.BoardAdminPropertyResponse;

/**
 * Step 2 of the volunteer-syndic wizard. "Self" zone like UserMeController:
 * the account is the authenticated principal, never a value of the payload.
 * Accepts the account-scoped onboarding token issued at step 1 as well as an
 * ordinary session - see PropertyAccessEvaluator.canCreateBoardAdminProperty.
 */
@RestController
@RequestMapping("/users/me")
public class BoardAdminOnboardingController {

    private final CreateBoardAdminPropertyUseCase createBoardAdminPropertyUseCase;
    private final JwtTokenPort jwtTokenPort;

    public BoardAdminOnboardingController(CreateBoardAdminPropertyUseCase createBoardAdminPropertyUseCase,
                                           JwtTokenPort jwtTokenPort) {
        this.createBoardAdminPropertyUseCase = createBoardAdminPropertyUseCase;
        this.jwtTokenPort = jwtTokenPort;
    }

    /**
     * Answers with a property-scoped onboarding token for steps 3 to 7, since
     * the account is not verified (hence cannot log in) yet.
     */
    @PreAuthorize("@propertyAccess.canCreateBoardAdminProperty(authentication)")
    @PostMapping("/board-admin-property")
    public ResponseEntity<BoardAdminPropertyResponse> createProperty(
            @Valid @RequestBody CreateBoardAdminPropertyRequest request, Authentication authentication) {
        UserId userId = UserId.of(authentication.getName());
        EntityId propertyId = createBoardAdminPropertyUseCase.create(new CreateBoardAdminPropertyCommand(
                userId, request.propertyName(), request.propertyAddress(), request.propertyCity(),
                request.boardRole()));
        String onboardingToken = jwtTokenPort.generateOnboardingToken(EntityId.of(userId.asUuid()), propertyId);
        return ResponseEntity.created(URI.create("/api/v1/properties/" + propertyId))
                .body(new BoardAdminPropertyResponse(propertyId.toString(), onboardingToken,
                        jwtTokenPort.onboardingTokenTtlSeconds()));
    }
}
