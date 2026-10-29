package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.application.port.out.AsyncEmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.RegisterPropertyBoardAdminCommand;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.testsupport.TestEmailTemplates;

@ExtendWith(MockitoExtension.class)
class RegisterPropertyBoardAdminServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-09T10:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private AsyncEmailSenderPort asyncEmailSender;

    private RegisterPropertyBoardAdminService newService() {
        return new RegisterPropertyBoardAdminService(userRepository, verificationTokenRepository, passwordEncoderPort,
                asyncEmailSender, new VerificationTokenGenerator(), new VerificationEmailComposer("http://localhost/verify", TestEmailTemplates.renderer()),
                Clock.fixed(NOW, ZoneOffset.UTC), 24L);
    }

    private static RegisterPropertyBoardAdminCommand command() {
        return new RegisterPropertyBoardAdminCommand(
                "Jane Doe", EmailVO.of("board-admin@oikos.com"), "+212612345678", RawPassword.of("password123"));
    }

    @Test
    void registering_creates_the_account_alone_stamped_with_its_terms_acceptance() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().register(command());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getRoles()).containsExactly(Role.ROLE_USER);
        // The property, its party and the board-admin grant all come at step 2.
        assertThat(saved.getLinkedPartyIds()).isEmpty();
        assertThat(saved.getPropertyRoleGrants()).isEmpty();
        assertThat(saved.getTermsAcceptedAt()).isEqualTo(NOW);
        assertThat(saved.isVerified()).isFalse();

        verify(verificationTokenRepository).save(any());
        verify(asyncEmailSender).send(any(), any(), any());
    }

    @Test
    void registering_with_an_email_already_in_use_creates_nothing() {
        when(userRepository.existsByEmail("board-admin@oikos.com")).thenReturn(true);

        assertThatThrownBy(() -> newService().register(command())).isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any());
        verify(asyncEmailSender, never()).send(any(), any(), any());
    }
}
