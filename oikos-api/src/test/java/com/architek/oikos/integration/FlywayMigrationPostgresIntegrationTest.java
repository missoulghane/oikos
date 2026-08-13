package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validates rule 10: Flyway is the source of truth of the schema in production
 * (PostgreSQL, ddl-auto=validate) - if V1__baseline.sql ever drifts from the JPA
 * entity mappings, Hibernate's schema validation fails the context startup here.
 */
class FlywayMigrationPostgresIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private DataSource dataSource;

    @Test
    void flyway_migrations_apply_cleanly_and_hibernate_validates_the_resulting_schema() {
        assertThat(dataSource).isNotNull();
    }
}
