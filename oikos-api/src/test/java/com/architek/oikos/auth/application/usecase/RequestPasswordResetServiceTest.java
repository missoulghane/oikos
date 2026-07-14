package com.architek.oikos.auth.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.architek.oikos.auth.application.command.RequestPasswordResetCommand;
import com.architek.oikos.auth.application.port.out.UserAccountPort;
import com.architek.oikos.auth.domain.repository.PasswordResetTokenRepository;
import com.architek.oikos.auth.domain.service.PasswordResetTokenGenerator;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetServiceTest {

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailSenderPort emailSenderPort;

    private RequestPasswordResetService newService() {
        return new RequestPasswordResetService(userAccountPort, passwordResetTokenRepository, emailSenderPort,
                new PasswordResetTokenGenerator(), new PasswordResetEmailComposer("http://localhost/reset-password"),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 1L);
    }

    @Test
    void known_email_gets_a_reset_token_and_an_email() {
        EntityId userId = EntityId.newId();
        when(userAccountPort.findIdByEmail(any())).thenReturn(Optional.of(userId));

        newService().requestReset(new RequestPasswordResetCommand(EmailVO.of("known@oikos.com")));

        verify(passwordResetTokenRepository).deleteByUserId(userId);
        verify(passwordResetTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }

    @Test
    void unknown_email_is_silently_ignored() {
        when(userAccountPort.findIdByEmail(any())).thenReturn(Optional.empty());

        newService().requestReset(new RequestPasswordResetCommand(EmailVO.of("unknown@oikos.com")));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailSenderPort, never()).send(any(), any(), any());
    }
}
