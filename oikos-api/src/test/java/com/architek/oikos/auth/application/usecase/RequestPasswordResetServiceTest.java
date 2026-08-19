package com.architek.oikos.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.auth.application.command.RequestPasswordResetCommand;
import com.architek.oikos.auth.application.port.out.UserAccountPort;
import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.auth.domain.repository.PasswordResetTokenRepository;
import com.architek.oikos.auth.domain.service.PasswordResetTokenGenerator;
import com.architek.oikos.shared.application.port.out.EmailSendQuotaPort;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.TooManyRequestsException;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetServiceTest {

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailSenderPort emailSenderPort;

    /** Plafond par adresse : laissé passant ici, il a son propre cas plus bas. */
    @Mock
    private EmailSendQuotaPort emailSendQuotaPort;

    private RequestPasswordResetService newService() {
        return new RequestPasswordResetService(userAccountPort, passwordResetTokenRepository, emailSenderPort,
                emailSendQuotaPort, new PasswordResetTokenGenerator(),
                new PasswordResetEmailComposer("http://localhost/reset-password"),
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
    void the_stored_token_is_the_hash_of_the_one_that_is_mailed() {
        // The contract that makes hashing worth anything: what sits in the database
        // must not be enough to rebuild the link that went out.
        PasswordResetTokenGenerator generator = new PasswordResetTokenGenerator();
        when(userAccountPort.findIdByEmail(any())).thenReturn(Optional.of(EntityId.newId()));
        ArgumentCaptor<PasswordResetToken> savedToken = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> sentBody = ArgumentCaptor.forClass(String.class);

        newService().requestReset(new RequestPasswordResetCommand(EmailVO.of("known@oikos.com")));

        verify(passwordResetTokenRepository).save(savedToken.capture());
        verify(emailSenderPort).send(any(), any(), sentBody.capture());
        String mailedToken = mailedTokenOf(sentBody.getValue());
        assertThat(savedToken.getValue().tokenHash())
                .isEqualTo(generator.hash(mailedToken))
                .isNotEqualTo(mailedToken);
        assertThat(sentBody.getValue()).doesNotContain(savedToken.getValue().tokenHash());
    }

    /** Reads the token back out of the mailed link - the one place it exists in clear. */
    private static String mailedTokenOf(String htmlBody) {
        Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)").matcher(htmlBody);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    @Test
    void the_per_address_quota_is_checked_before_the_account_is_even_looked_up() {
        // Appliqué après, le refus ne frapperait que les adresses connues et
        // répondrait, par son seul existence, à la question que ce endpoint passe
        // le reste de son code à ne pas répondre.
        doThrow(new TooManyRequestsException("trop tôt", Duration.ofMinutes(5)))
                .when(emailSendQuotaPort).requireQuota("known@oikos.com");

        assertThatThrownBy(() -> newService().requestReset(new RequestPasswordResetCommand(EmailVO.of("known@oikos.com"))))
                .isInstanceOf(TooManyRequestsException.class);

        verifyNoInteractions(userAccountPort);
        verify(emailSenderPort, never()).send(any(), any(), any());
    }

    @Test
    void unknown_email_is_silently_ignored() {
        when(userAccountPort.findIdByEmail(any())).thenReturn(Optional.empty());

        newService().requestReset(new RequestPasswordResetCommand(EmailVO.of("unknown@oikos.com")));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailSenderPort, never()).send(any(), any(), any());
    }
}
