package com.architek.oikos.auth.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.auth.application.command.LogoutCommand;
import com.architek.oikos.auth.application.port.in.LogoutUseCase;
import com.architek.oikos.auth.domain.repository.RefreshTokenRepository;
import com.architek.oikos.auth.domain.service.RefreshTokenSecretGenerator;

@Component
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenSecretGenerator secretGenerator;

    public LogoutService(RefreshTokenRepository refreshTokenRepository, RefreshTokenSecretGenerator secretGenerator) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secretGenerator = secretGenerator;
    }

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        String hash = secretGenerator.hash(command.refreshToken());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> refreshTokenRepository.save(token.revoke()));
    }
}
