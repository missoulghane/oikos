package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.ResendAccountActivationCommand;
import com.architek.oikos.user.application.port.out.PartyDetails;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;
import com.architek.oikos.user.domain.exception.AccountAlreadyVerifiedException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class ResendAccountActivationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailSenderPort emailSenderPort;

    private ResendAccountActivationService newService() {
        return new ResendAccountActivationService(userRepository, partyDirectoryPort, verificationTokenRepository, emailSenderPort,
                new VerificationTokenGenerator(), new AccountActivationEmailComposer("http://localhost/activate-account"),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 24L);
    }

    private static User newAdminCreatedUser() {
        return User.registerByAdmin(UserId.newId(), EntityId.newId(), HashedPassword.of("placeholder"), null);
    }

    @Test
    void resending_for_an_unverified_user_issues_a_new_token_and_sends_an_email() {
        User user = newAdminCreatedUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(partyDirectoryPort.getPartyById(user.getPartyId()))
                .thenReturn(new PartyDetails("Jane Doe", EmailVO.of("invited@oikos.com"), null));

        newService().resend(new ResendAccountActivationCommand(user.getId()));

        verify(verificationTokenRepository).deleteByUserId(user.getId());
        verify(verificationTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }

    @Test
    void resending_for_an_already_verified_user_is_rejected() {
        User user = newAdminCreatedUser().verify();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> newService().resend(new ResendAccountActivationCommand(user.getId())))
                .isInstanceOf(AccountAlreadyVerifiedException.class);
    }

    @Test
    void resending_for_an_unknown_user_is_rejected() {
        UserId id = UserId.newId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().resend(new ResendAccountActivationCommand(id)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
