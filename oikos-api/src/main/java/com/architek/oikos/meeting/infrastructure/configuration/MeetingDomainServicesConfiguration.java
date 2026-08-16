package com.architek.oikos.meeting.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.architek.oikos.meeting.domain.service.ConvocationTokenGenerator;
import com.architek.oikos.meeting.domain.service.ShortCodeGenerator;

/**
 * Wires pure-domain services (which cannot carry Spring annotations) as beans.
 */
@Configuration
public class MeetingDomainServicesConfiguration {

    @Bean
    public ConvocationTokenGenerator convocationTokenGenerator() {
        return new ConvocationTokenGenerator();
    }

    @Bean
    public ShortCodeGenerator shortCodeGenerator() {
        return new ShortCodeGenerator();
    }
}
