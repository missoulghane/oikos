package com.architek.oikos.shared.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Single source of truth for the BCrypt PasswordEncoder bean, used both by Spring
 * Security's authentication provider (auth feature) and by user's PasswordEncoderPort
 * adapter, so both rely on the exact same hashing algorithm/strength.
 */
@Configuration
public class PasswordEncoderConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
