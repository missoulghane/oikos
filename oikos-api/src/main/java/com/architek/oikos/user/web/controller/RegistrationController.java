package com.architek.oikos.user.web.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.AcceptPartyInvitationCommand;
import com.architek.oikos.user.application.command.ActivateAccountCommand;
import com.architek.oikos.user.application.command.CaptureOnboardingLeadCommand;
import com.architek.oikos.user.application.command.RegisterPropertyBoardAdminCommand;
import com.architek.oikos.user.application.command.RegisterPropertyManagerAdminCommand;
import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.application.command.ResendVerificationCommand;
import com.architek.oikos.user.application.command.VerifyAccountCommand;
import com.architek.oikos.user.application.port.in.AcceptPartyInvitationUseCase;
import com.architek.oikos.user.application.port.in.ActivateAccountUseCase;
import com.architek.oikos.user.application.port.in.CaptureOnboardingLeadUseCase;
import com.architek.oikos.user.application.port.in.RegisterPropertyBoardAdminUseCase;
import com.architek.oikos.user.application.port.in.RegisterPropertyManagerAdminUseCase;
import com.architek.oikos.user.application.port.in.RegisterUserUseCase;
import com.architek.oikos.user.application.port.in.ResendVerificationUseCase;
import com.architek.oikos.user.application.port.in.VerifyAccountUseCase;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.web.request.AcceptInvitationRequest;
import com.architek.oikos.user.web.request.ActivateAccountRequest;
import com.architek.oikos.user.web.request.CaptureOnboardingLeadRequest;
import com.architek.oikos.user.web.request.RegisterPropertyBoardAdminRequest;
import com.architek.oikos.user.web.request.RegisterPropertyManagerAdminRequest;
import com.architek.oikos.user.web.request.RegisterUserRequest;
import com.architek.oikos.user.web.request.ResendVerificationRequest;
import com.architek.oikos.user.web.request.VerifyAccountRequest;
import com.architek.oikos.user.web.response.MessageResponse;
import com.architek.oikos.user.web.response.RegisteredBoardAdminResponse;
import com.architek.oikos.user.application.dto.RegisteredBoardAdminView;
import com.architek.oikos.auth.application.port.out.JwtTokenPort;

/**
 * Public endpoints: account creation and activation. No authentication required.
 */
@RestController
@RequestMapping("/users")
public class RegistrationController {

    private final RegisterUserUseCase registerUserUseCase;
    private final RegisterPropertyBoardAdminUseCase registerPropertyBoardAdminUseCase;
    private final RegisterPropertyManagerAdminUseCase registerPropertyManagerAdminUseCase;
    private final VerifyAccountUseCase verifyAccountUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;
    private final AcceptPartyInvitationUseCase acceptPartyInvitationUseCase;
    private final CaptureOnboardingLeadUseCase captureOnboardingLeadUseCase;
    private final JwtTokenPort jwtTokenPort;

    public RegistrationController(RegisterUserUseCase registerUserUseCase,
                                   RegisterPropertyBoardAdminUseCase registerPropertyBoardAdminUseCase,
                                   RegisterPropertyManagerAdminUseCase registerPropertyManagerAdminUseCase,
                                   VerifyAccountUseCase verifyAccountUseCase,
                                   ResendVerificationUseCase resendVerificationUseCase,
                                   ActivateAccountUseCase activateAccountUseCase,
                                   AcceptPartyInvitationUseCase acceptPartyInvitationUseCase,
                                   CaptureOnboardingLeadUseCase captureOnboardingLeadUseCase,
                                   JwtTokenPort jwtTokenPort) {
        this.registerUserUseCase = registerUserUseCase;
        this.registerPropertyBoardAdminUseCase = registerPropertyBoardAdminUseCase;
        this.registerPropertyManagerAdminUseCase = registerPropertyManagerAdminUseCase;
        this.verifyAccountUseCase = verifyAccountUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
        this.activateAccountUseCase = activateAccountUseCase;
        this.acceptPartyInvitationUseCase = acceptPartyInvitationUseCase;
        this.captureOnboardingLeadUseCase = captureOnboardingLeadUseCase;
        this.jwtTokenPort = jwtTokenPort;
    }

    /**
     * Step 1 of the volunteer-syndic wizard. Always 202: the response must not
     * reveal whether the address is already known (see CaptureOnboardingLeadService),
     * and the visitor's next step must never be blocked by this side effect.
     */
    @PostMapping("/onboarding-leads")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void captureOnboardingLead(@Valid @RequestBody CaptureOnboardingLeadRequest request) {
        captureOnboardingLeadUseCase.capture(new CaptureOnboardingLeadCommand(
                EmailVO.of(request.email()), request.fullName()));
    }

    @PostMapping("/register-property-user")
    public ResponseEntity<Void> registerPropertyUser(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.fullName(),
                EmailVO.of(request.email()),
                request.phone(),
                RawPassword.of(request.password()),
                request.role(),
                request.returnTo(),
                request.invitationToken(),
                request.unitId() != null ? EntityId.of(request.unitId()) : null);
        UserId userId = registerUserUseCase.register(command);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId)).build();
    }

    /**
     * Volunteer syndic board admin - self-managed HOA. Called at the end of
     * step 2 of the wizard, as soon as the property has a name: the Party
     * linking the account to its property cannot exist before that.
     * Answers with the onboarding token the remaining steps need, since the
     * account is not verified (hence cannot log in) yet.
     */
    @PostMapping("/register-property-board-admin")
    public ResponseEntity<RegisteredBoardAdminResponse> registerPropertyBoardAdmin(
            @Valid @RequestBody RegisterPropertyBoardAdminRequest request) {
        RegisterPropertyBoardAdminCommand command = new RegisterPropertyBoardAdminCommand(
                request.fullName(),
                EmailVO.of(request.email()),
                request.phone(),
                RawPassword.of(request.password()),
                request.propertyName(),
                request.propertyAddress(), request.propertyCity());
        RegisteredBoardAdminView registration = registerPropertyBoardAdminUseCase.register(command);
        String onboardingToken = jwtTokenPort.generateOnboardingToken(
                EntityId.of(registration.userId().asUuid()), registration.propertyId());
        return ResponseEntity.created(URI.create("/api/v1/users/" + registration.userId()))
                .body(RegisteredBoardAdminResponse.from(registration, onboardingToken,
                        jwtTokenPort.onboardingTokenTtlSeconds()));
    }

    /** Professional property-management firm admin - uncapped, may create further properties. */
    @PostMapping("/register-property-manager-admin")
    public ResponseEntity<Void> registerPropertyManagerAdmin(@Valid @RequestBody RegisterPropertyManagerAdminRequest request) {
        RegisterPropertyManagerAdminCommand command = new RegisterPropertyManagerAdminCommand(
                request.fullName(),
                EmailVO.of(request.email()),
                request.phone(),
                RawPassword.of(request.password()),
                request.propertyName(),
                request.propertyAddress(), request.propertyCity());
        UserId userId = registerPropertyManagerAdminUseCase.register(command);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId)).build();
    }

    @PostMapping("/verify")
    public ResponseEntity<MessageResponse> verify(@Valid @RequestBody VerifyAccountRequest request) {
        verifyAccountUseCase.verify(new VerifyAccountCommand(request.token()));
        return ResponseEntity.ok(new MessageResponse("Account verified successfully"));
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/resend-verification")
    public void resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        resendVerificationUseCase.resend(new ResendVerificationCommand(EmailVO.of(request.email())));
    }

    @PostMapping("/activate-account")
    public ResponseEntity<MessageResponse> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        activateAccountUseCase.activate(new ActivateAccountCommand(request.token(), RawPassword.of(request.newPassword())));
        return ResponseEntity.ok(new MessageResponse("Account activated successfully"));
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<MessageResponse> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        acceptPartyInvitationUseCase.accept(new AcceptPartyInvitationCommand(request.token(), request.password()));
        return ResponseEntity.ok(new MessageResponse("Invitation accepted successfully"));
    }
}
