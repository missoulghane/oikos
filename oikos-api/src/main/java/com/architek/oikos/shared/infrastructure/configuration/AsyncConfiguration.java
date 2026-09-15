package com.architek.oikos.shared.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Turns on Spring's @Async support, used to send the verification email
 * fire-and-forget at the end of registration (see AsyncEmailSender) instead
 * of making the caller wait on the SMTP round-trip.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
