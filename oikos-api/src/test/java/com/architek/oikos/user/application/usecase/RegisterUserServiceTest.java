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

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.application.port.out.MembershipRequestSubmissionPort;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
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
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private MembershipRequestSubmissionPort membershipRequestSubmissionPort;

    private RegisterUserService newService() {
        return new RegisterUserService(userRepository, verificationTokenRepository, passwordEncoderPort,
                emailSenderPort, new VerificationTokenGenerator(), new VerificationEmailComposer("http://localhost/verify"),
                membershipRequestSubmissionPort, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 24L);
    }

    @Test
    void registering_a_new_email_persists_the_user_and_sends_a_verification_email() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("new@oikos.com"), "+212612345678", RawPassword.of("password123"), null, null, null, null);

        newService().register(command);

        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }

    @Test
    void the_phone_given_at_registration_lands_on_the_account() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("new@oikos.com"), "+212612345678", RawPassword.of("password123"), null, null, null, null);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPhone()).isEqualTo("+212612345678");
    }

    @Test
    void an_account_created_without_a_phone_stays_valid() {
        // Le formulaire l'exige, l'API non : les comptes déjà en base n'en ont pas,
        // et les invitations en créent sans jamais en demander.
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("new@oikos.com"), null, RawPassword.of("password123"), null, null, null, null);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPhone()).isNull();
    }

    @Test
    void registering_an_already_used_email_is_rejected() {
        when(userRepository.existsByEmail("existing@oikos.com")).thenReturn(true);

        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("existing@oikos.com"), "+212612345678", RawPassword.of("password123"), null, null, null, null);

        assertThatThrownBy(() -> newService().register(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void role_defaults_to_user_when_not_specified() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("new@oikos.com"), "+212612345678", RawPassword.of("password123"), null, null, null, null);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    void role_is_assigned_when_explicitly_registrable() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("new@oikos.com"), "+212612345678", RawPassword.of("password123"), Role.ROLE_USER, null, null, null);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    void a_privileged_role_cannot_be_self_assigned_at_registration() {
        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("new@oikos.com"), "+212612345678", RawPassword.of("password123"), Role.ROLE_ADMIN, null, null, null);

        assertThatThrownBy(() -> newService().register(command)).isInstanceOf(RoleNotAllowedException.class);
    }

    @Test
    void password_is_hashed_before_persisting_the_user() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed-value"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                "Jane Doe", EmailVO.of("new@oikos.com"), "+212612345678", RawPassword.of("password123"), null, null, null, null);

        newService().register(command);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword().value()).isEqualTo("hashed-value");
    }

    @Test
    void a_valid_relative_return_to_is_embedded_in_the_verification_email() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand("Jane Doe", EmailVO.of("new@oikos.com"),
                "+212612345678", RawPassword.of("password123"), null, "/invitations?token=abc&unitId=def", null, null);

        newService().register(command);

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("returnTo=%2Finvitations%3Ftoken%3Dabc%26unitId%3Ddef");
    }

    @Test
    void a_protocol_relative_return_to_is_dropped_to_prevent_an_open_redirect() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand("Jane Doe", EmailVO.of("new@oikos.com"),
                "+212612345678", RawPassword.of("password123"), null, "//evil.com", null, null);

        newService().register(command);

        var bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).send(any(), any(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).doesNotContain("returnTo").doesNotContain("evil.com");
    }

    @Test
    void registering_through_a_public_invitation_submits_the_membership_request_in_the_same_call() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        EntityId unitId = EntityId.newId();

        RegisterUserCommand command = new RegisterUserCommand("Jane Doe", EmailVO.of("new@oikos.com"),
                "+212612345678", RawPassword.of("password123"), null, null, "inv-token", unitId);

        newService().register(command);

        var userIdCaptor = org.mockito.ArgumentCaptor.forClass(com.architek.oikos.user.domain.valueobject.UserId.class);
        verify(membershipRequestSubmissionPort).submit(org.mockito.ArgumentMatchers.eq("inv-token"), userIdCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(unitId));
        assertThat(userIdCaptor.getValue()).isNotNull();
        verify(emailSenderPort).send(any(), any(), any());
    }

    @Test
    void registration_is_rolled_back_when_the_invitation_can_no_longer_be_submitted() {
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(new IllegalArgumentException("invitation no longer usable"))
                .when(membershipRequestSubmissionPort).submit(any(), any(), any());

        RegisterUserCommand command = new RegisterUserCommand("Jane Doe", EmailVO.of("new@oikos.com"),
                "+212612345678", RawPassword.of("password123"), null, null, "inv-token", EntityId.newId());

        assertThatThrownBy(() -> newService().register(command)).isInstanceOf(IllegalArgumentException.class);

        verify(verificationTokenRepository, org.mockito.Mockito.never()).save(any());
        verify(emailSenderPort, org.mockito.Mockito.never()).send(any(), any(), any());
    }
}
