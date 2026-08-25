package com.architek.oikos.shared.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns on Spring's @Scheduled support, used by the nightly imputation of
 * advances onto echeances that have fallen due
 * (DueInstallmentRegularizationScheduler).
 */
@Configuration
@EnableScheduling
public class SchedulingConfiguration {
}
