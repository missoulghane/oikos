package com.architek.oikos.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for the tests that must run against a real PostgreSQL rather than
 * the H2 instance the rest of the suite uses: H2 in {@code MODE=PostgreSQL} is
 * loosely typed and silently accepts SQL that PostgreSQL rejects, so anything
 * depending on PostgreSQL's actual type resolution has to be asserted here.
 *
 * <p>The container is a JVM-wide singleton started in a static initialiser
 * instead of a per-class {@code @Container}: every subclass then shares the
 * exact same property values, the Spring context stays cacheable across them,
 * and one container serves the whole run. Ryuk stops it when the JVM exits.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("prod")
abstract class PostgresIntegrationTestBase {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

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
}
