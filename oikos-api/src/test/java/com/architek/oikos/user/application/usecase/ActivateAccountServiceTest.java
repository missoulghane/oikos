package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.ActivateAccountCommand;
import com.architek.oikos.user.domain.exception.InvalidVerificationTokenException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class ActivateAccountServiceTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private ActivateAccountService newService() {
        return new ActivateAccountService(verificationTokenRepository, userRepository, passwordEncoderPort, clock);
    }

    private static User newAdminCreatedUser() {
        return User.registerByAdmin(UserId.newId(), EmailVO.of("invited@oikos.com"), "Jane Doe", HashedPassword.of("placeholder"));
    }

    @Test
    void activating_with_a_valid_token_verifies_the_account_and_sets_the_chosen_password() {
        User user = newAdminCreatedUser();
        VerificationToken token = VerificationToken.issue(user.getId(), "the-token", clock.instant().plusSeconds(3600));
        when(verificationTokenRepository.findByToken("the-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("new-hashed-password"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().activate(new ActivateAccountCommand("the-token", RawPassword.of("newpassword123")));

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isVerified()).isTrue();
        assertThat(captor.getValue().getPassword().value()).isEqualTo("new-hashed-password");
        verify(verificationTokenRepository).deleteByUserId(user.getId());
    }

    @Test
    void activating_with_an_unknown_token_is_rejected() {
        when(verificationTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().activate(new ActivateAccountCommand("bad-token", RawPassword.of("newpassword123"))))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }

    @Test
    void activating_with_an_expired_token_is_rejected() {
        User user = newAdminCreatedUser();
        VerificationToken expiredToken = VerificationToken.issue(user.getId(), "expired-token", clock.instant().minusSeconds(1));
        when(verificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> newService().activate(new ActivateAccountCommand("expired-token", RawPassword.of("newpassword123"))))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }
}
