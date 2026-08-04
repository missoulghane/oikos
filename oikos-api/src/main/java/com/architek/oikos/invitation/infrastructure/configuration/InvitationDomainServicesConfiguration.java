package com.architek.oikos.invitation.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.architek.oikos.invitation.domain.service.InvitationTokenGenerator;

/**
 * Wires pure-domain services (which cannot carry Spring annotations) as beans.
 */
@Configuration
public class InvitationDomainServicesConfiguration {

    @Bean
    public InvitationTokenGenerator invitationTokenGenerator() {
        return new InvitationTokenGenerator();
    }
}
