package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.CreateUserCommand;
import com.architek.oikos.user.application.port.out.ContactDirectoryPort;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactDirectoryPort contactDirectoryPort;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    private CreateUserService newService() {
        return new CreateUserService(userRepository, contactDirectoryPort, verificationTokenRepository, passwordEncoderPort,
                emailSenderPort, new VerificationTokenGenerator(),
                new AccountActivationEmailComposer("http://localhost/activate-account"),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 24L);
    }

    @Test
    void creating_a_user_persists_an_unverified_but_enabled_account_and_sends_an_activation_email() {
        when(contactDirectoryPort.createContact(any())).thenReturn(EntityId.newId());
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateUserCommand command = new CreateUserCommand("Doe", "Jane", EmailVO.of("admin-created@oikos.com"), null, null);

        newService().create(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isVerified()).isFalse();
        assertThat(captor.getValue().isEnabled()).isTrue();
        verify(verificationTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }

    @Test
    void creating_a_user_with_an_already_used_email_is_rejected() {
        when(contactDirectoryPort.createContact(any()))
                .thenThrow(new EmailAlreadyUsedException("existing@oikos.com"));

        CreateUserCommand command = new CreateUserCommand("Doe", "Jane", EmailVO.of("existing@oikos.com"), null, null);

        assertThatThrownBy(() -> newService().create(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }
}
