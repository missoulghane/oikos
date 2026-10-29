package com.architek.oikos.shared.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on @Scheduled jobs; each job decides on its own whether it is enabled. */
@Configuration
@EnableScheduling
public class SchedulingConfiguration {
}
