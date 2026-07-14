package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Validates rule 10: Flyway is the source of truth of the schema in production
 * (PostgreSQL, ddl-auto=validate). This is the only test running against a real
 * PostgreSQL instance (via Testcontainers) with Flyway enabled - if V1__baseline.sql
 * ever drifts from the JPA entity mappings, Hibernate's schema validation fails the
 * context startup here.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("prod")
class FlywayMigrationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        // Explicit overrides rather than relying on @ActiveProfiles("prod") alone:
        // src/test/resources/application.yml (H2, Flyway disabled) is always on the
        // test classpath too, so every property this validation depends on is set
        // here directly to remove any ambiguity about profile-precedence ordering.
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private DataSource dataSource;

    @Test
    void flyway_migrations_apply_cleanly_and_hibernate_validates_the_resulting_schema() {
        assertThat(dataSource).isNotNull();
    }
}
