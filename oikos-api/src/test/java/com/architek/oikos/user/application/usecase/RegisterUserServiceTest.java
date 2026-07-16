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
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.application.port.out.ContactDirectoryPort;
import com.architek.oikos.user.domain.exception.RoleNotAllowedException;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

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

    private RegisterUserService newService() {
        return new RegisterUserService(userRepository, contactDirectoryPort, verificationTokenRepository, passwordEncoderPort,
                emailSenderPort, new VerificationTokenGenerator(), new VerificationEmailComposer("http://localhost/verify"),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 24L);
    }

    @Test
    void registering_a_new_email_persists_the_user_and_sends_a_verification_email() {
        when(contactDirectoryPort.createContact(any())).thenReturn(EntityId.newId());
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Doe", "Jane", EmailVO.of("new@oikos.com"), null, null, RawPassword.of("password123"), null);

        newService().register(command);

        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }

    @Test
    void registering_an_already_used_email_is_rejected() {
        when(contactDirectoryPort.createContact(any())).thenThrow(new EmailAlreadyUsedException("existing@oikos.com"));

        RegisterUserCommand command = new RegisterUserCommand(
                "Doe", "Jane", EmailVO.of("existing@oikos.com"), null, null, RawPassword.of("password123"), null);

        assertThatThrownBy(() -> newService().register(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void role_defaults_to_user_when_not_specified() {
        when(contactDirectoryPort.createContact(any())).thenReturn(EntityId.newId());
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Doe", "Jane", EmailVO.of("new@oikos.com"), null, null, RawPassword.of("password123"), null);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    void role_is_assigned_when_it_is_registrable() {
        when(contactDirectoryPort.createContact(any())).thenReturn(EntityId.newId());
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Doe", "Jane", EmailVO.of("new@oikos.com"), null, null, RawPassword.of("password123"),
                Role.ROLE_PROPERTY_MANAGER);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).containsExactly(Role.ROLE_PROPERTY_MANAGER);
    }

    @Test
    void a_privileged_role_cannot_be_self_assigned_at_registration() {
        RegisterUserCommand command = new RegisterUserCommand(
                "Doe", "Jane", EmailVO.of("new@oikos.com"), null, null, RawPassword.of("password123"), Role.ROLE_ADMIN);

        assertThatThrownBy(() -> newService().register(command)).isInstanceOf(RoleNotAllowedException.class);
    }

    @Test
    void password_is_hashed_before_persisting_the_user() {
        when(contactDirectoryPort.createContact(any())).thenReturn(EntityId.newId());
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed-value"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Doe", "Jane", EmailVO.of("new@oikos.com"), null, null, RawPassword.of("password123"), null);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword().value()).isEqualTo("hashed-value");
    }
}
