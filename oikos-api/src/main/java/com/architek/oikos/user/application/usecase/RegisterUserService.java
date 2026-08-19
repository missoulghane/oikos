package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.application.port.in.RegisterUserUseCase;
import com.architek.oikos.user.application.port.out.MembershipRequestSubmissionPort;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.exception.RoleNotAllowedException;
import com.architek.oikos.user.domain.model.RegistrableRoles;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

@Component
public class RegisterUserService implements RegisterUserUseCase {

    // A single leading '/' not followed by another '/': rules out "//evil.com"
    // (browser-parsed as protocol-relative, an open-redirect vector) and any
    // absolute URL, while still allowing a real relative in-app path.
    private static final Pattern RETURN_TO_PATTERN = Pattern.compile("^/(?!/).*");

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final MembershipRequestSubmissionPort membershipRequestSubmissionPort;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public RegisterUserService(UserRepository userRepository,
                                VerificationTokenRepository verificationTokenRepository,
                                PasswordEncoderPort passwordEncoderPort,
                                EmailSenderPort emailSenderPort,
                                VerificationTokenGenerator tokenGenerator,
                                VerificationEmailComposer emailComposer,
                                MembershipRequestSubmissionPort membershipRequestSubmissionPort,
                                Clock clock,
                                @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.emailSenderPort = emailSenderPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.membershipRequestSubmissionPort = membershipRequestSubmissionPort;
        this.clock = clock;
        this.verificationTokenTtl = Duration.ofHours(verificationTokenTtlHours);
    }

    @Override
    @Transactional
    public UserId register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email().value())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        Role role = command.role() != null ? command.role() : Role.ROLE_USER;
        if (!RegistrableRoles.isAllowed(role)) {
            throw new RoleNotAllowedException(role);
        }
        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), command.email(), command.fullName(), command.phone(),
                hashedPassword, role);
        User savedUser = userRepository.save(user);

        if (command.invitationToken() != null) {
            // Same transaction as the account creation above: an invalid/expired
            // invitation at this point fails the whole registration rather than
            // leaving an account with a silently-dropped membership request -
            // the visitor sees the error immediately and can retry, instead of
            // discovering it only after verifying their email.
            membershipRequestSubmissionPort.submit(command.invitationToken(), savedUser.getId(), command.unitId());
        }

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken verificationToken = VerificationToken.issue(savedUser.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(verificationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(),
                emailComposer.htmlBody(rawToken, sanitizeReturnTo(command.returnTo())));
        return savedUser.getId();
    }

    private static String sanitizeReturnTo(String returnTo) {
        if (returnTo == null || !RETURN_TO_PATTERN.matcher(returnTo).matches()) {
            return null;
        }
        return returnTo;
    }
}
