package com.architek.oikos.user.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.architek.oikos.user.domain.service.PartyInvitationTokenGenerator;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;

/**
 * Wires pure-domain services (which cannot carry Spring annotations) as beans.
 */
@Configuration
public class UserDomainServicesConfiguration {

    @Bean
    public VerificationTokenGenerator verificationTokenGenerator() {
        return new VerificationTokenGenerator();
    }

    @Bean
    public PartyInvitationTokenGenerator partyInvitationTokenGenerator() {
        return new PartyInvitationTokenGenerator();
    }
}
