package com.architek.oikos.auth.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.architek.oikos.auth.domain.service.PasswordResetTokenGenerator;
import com.architek.oikos.auth.domain.service.RefreshTokenSecretGenerator;

@Configuration
public class AuthDomainServicesConfiguration {

    @Bean
    public RefreshTokenSecretGenerator refreshTokenSecretGenerator() {
        return new RefreshTokenSecretGenerator();
    }

    @Bean
    public PasswordResetTokenGenerator passwordResetTokenGenerator() {
        return new PasswordResetTokenGenerator();
    }
}
