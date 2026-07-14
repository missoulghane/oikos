package com.architek.oikos.shared.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables @CreatedDate/@LastModifiedDate population for every AuditableEntity.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
