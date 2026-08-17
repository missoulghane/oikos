package com.architek.oikos.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Runs every seed script the dev profile loads against a Hibernate-generated H2
 * schema - which is exactly what the dev profile is, and exactly what nothing
 * else covers.
 *
 * <p>The gap this closes is narrow and was expensive. The PostgreSQL integration
 * tests apply the migrations through Flyway, so they exercise the prod path;
 * ConvocationChannelSeedTest and ReplyMediumSeedTest read the files as text, so
 * they catch a catalog that drifts between the two copies. Neither one ever
 * <em>executes</em> a seed against H2. A seed can therefore be correct
 * PostgreSQL, listed in application-dev.yml, agree with its migration - and
 * still fail on start-up, which is how {@code "position"} shipped: quoted, it is
 * a case-sensitive lookup for a lower-case column, and Hibernate had generated
 * POSITION. The application simply refused to boot in dev.
 *
 * <p>Discovered from application-dev.yml rather than listed here, so a seed
 * added tomorrow and forgotten is covered the day it is registered. db/dev/dev.sql
 * is excluded: it is demo data with its own ordering and foreign keys, not a
 * catalog, and its failure would say nothing about the schema. Since the
 * 2026-08-17 squash these scripts live in db/seed/ rather than db/migration/ -
 * the single remaining migration is V1__baseline.sql, which this profile cannot
 * run at all.
 *
 * <p>Replayability is deliberately NOT asserted here, although every seed is
 * written to be replayable - the guards are what let an environment that
 * predates the squash re-run them harmlessly. On this side the schema is
 * create-drop, so the seeds always meet empty tables, and running one twice here
 * would only measure how H2 reads a FROM-less SELECT.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DevSeedScriptsRunOnH2Test {

    private static final Path DEV_PROFILE =
            resolveModuleRoot().resolve("src/main/resources/application-dev.yml");

    @Autowired
    private DataSource dataSource;

    @Test
    void every_seed_the_dev_profile_loads_executes_on_a_generated_schema() throws IOException {
        List<String> seeds = seedScripts();
        assertThat(seeds).as("application-dev.yml must list its seed scripts - none found").isNotEmpty();

        for (String seed : seeds) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource(seed));
            populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
            assertThatCode(() -> populator.execute(dataSource))
                    .as("%s must run against the schema Hibernate generates for dev - a quoted identifier or a "
                            + "PostgreSQL-only construct here stops the application from starting", seed)
                    .doesNotThrowAnyException();
        }
    }

    /** The db/seed scripts listed in spring.sql.init.data-locations, demo data excluded. */
    private static List<String> seedScripts() throws IOException {
        Matcher matcher = Pattern.compile("data-locations:\\s*(.+)").matcher(Files.readString(DEV_PROFILE));
        if (!matcher.find()) {
            throw new IllegalStateException("No spring.sql.init.data-locations in " + DEV_PROFILE.getFileName());
        }
        return Arrays.stream(matcher.group(1).split(","))
                .map(String::trim)
                .map(location -> location.replaceFirst("^classpath:", ""))
                .filter(location -> location.startsWith("db/seed/"))
                .toList();
    }

    private static Path resolveModuleRoot() {
        try {
            Path testClasses = Path.of(
                    DevSeedScriptsRunOnH2Test.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // testClasses points at <module-root>/target/test-classes
            return testClasses.getParent().getParent();
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException("Could not resolve module root for the dev profile", e);
        }
    }
}
