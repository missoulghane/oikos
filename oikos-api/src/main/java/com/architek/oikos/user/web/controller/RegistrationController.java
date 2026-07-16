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
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.ActivateAccountCommand;
import com.architek.oikos.user.application.command.RegisterPropertyManagerCommand;
import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.application.command.ResendVerificationCommand;
import com.architek.oikos.user.application.command.VerifyAccountCommand;
import com.architek.oikos.user.application.port.in.ActivateAccountUseCase;
import com.architek.oikos.user.application.port.in.RegisterPropertyManagerUseCase;
import com.architek.oikos.user.application.port.in.RegisterUserUseCase;
import com.architek.oikos.user.application.port.in.ResendVerificationUseCase;
import com.architek.oikos.user.application.port.in.VerifyAccountUseCase;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.web.request.ActivateAccountRequest;
import com.architek.oikos.user.web.request.RegisterPropertyManagerRequest;
import com.architek.oikos.user.web.request.RegisterUserRequest;
import com.architek.oikos.user.web.request.ResendVerificationRequest;
import com.architek.oikos.user.web.request.VerifyAccountRequest;
import com.architek.oikos.user.web.response.MessageResponse;

/**
 * Public endpoints: account creation and activation. No authentication required.
 */
@RestController
@RequestMapping("/users")
public class RegistrationController {

    private final RegisterUserUseCase registerUserUseCase;
    private final RegisterPropertyManagerUseCase registerPropertyManagerUseCase;
    private final VerifyAccountUseCase verifyAccountUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;

    public RegistrationController(RegisterUserUseCase registerUserUseCase,
                                   RegisterPropertyManagerUseCase registerPropertyManagerUseCase,
                                   VerifyAccountUseCase verifyAccountUseCase,
                                   ResendVerificationUseCase resendVerificationUseCase,
                                   ActivateAccountUseCase activateAccountUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.registerPropertyManagerUseCase = registerPropertyManagerUseCase;
        this.verifyAccountUseCase = verifyAccountUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
        this.activateAccountUseCase = activateAccountUseCase;
    }

    @PostMapping("/register-property-user")
    public ResponseEntity<Void> registerPropertyUser(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.lastName(),
                request.firstName(),
                EmailVO.of(request.email()),
                request.phone(),
                request.login(),
                RawPassword.of(request.password()),
                request.role());
        UserId userId = registerUserUseCase.register(command);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userId)).build();
    }

    @PostMapping("/register-property-manager")
    public ResponseEntity<Void> registerPropertyManager(@Valid @RequestBody RegisterPropertyManagerRequest request) {
        RegisterPropertyManagerCommand command = new RegisterPropertyManagerCommand(
                request.lastName(),
                request.firstName(),
                EmailVO.of(request.email()),
                request.phone(),
                request.login(),
                RawPassword.of(request.password()),
                request.propertyName(),
                request.propertyAddress());
        UserId userId = registerPropertyManagerUseCase.register(command);
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
}
