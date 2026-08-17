package com.architek.oikos.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;


/**
 * Guards the reply_medium catalog, which is seeded twice on purpose and so can
 * drift. Exact counterpart of {@link ConvocationChannelSeedTest}, one catalog
 * along.
 *
 * <p>V1__baseline.sql creates the table and fills it, for prod and docker. The dev profile
 * cannot apply V1 - it runs on H2 with Flyway disabled and a schema generated
 * by Hibernate, so a migration dropping and recreating constraints would fail on
 * the first statement - and loads db/seed/ as plain scripts through spring.sql.init
 * instead. Two files, one catalog: a medium added to V1 alone works in prod and
 * leaves the "reçue par" select box empty in dev.
 *
 * <p>Note what is NOT asserted here, unlike the channels: no Java constant has
 * to exist as a row. Nothing branches on a medium - it is recorded and
 * displayed, never emitted through - so the catalog can grow or shrink without
 * breaking a code path.
 */
class ReplyMediumSeedTest {

    private static final Path RESOURCES = resolveModuleRoot().resolve("src/main/resources");
    private static final Path CATALOG_MIGRATION = RESOURCES.resolve("db/migration/V1__baseline.sql");
    private static final Path SEED_MIGRATION = RESOURCES.resolve("db/seed/seed_reply_medium.sql");
    private static final Path DEV_PROFILE = RESOURCES.resolve("application-dev.yml");

    @Test
    void both_files_declare_the_same_catalog() throws IOException {
        assertThat(mediaIn(SEED_MIGRATION))
                .as("db/seed/ re-seeds for dev what V1__baseline.sql seeds for prod - a medium in one and not the other is a "
                        + "select box that differs between environments")
                .containsExactlyInAnyOrderElementsOf(mediaIn(CATALOG_MIGRATION));
    }

    @Test
    void the_dev_profile_loads_the_seed() throws IOException {
        assertThat(Files.readString(DEV_PROFILE))
                .as("application-dev.yml must list the seed file in spring.sql.init.data-locations - without it the dev "
                        + "database offers no medium at all when recording an answer taken at the office")
                .contains(SEED_MIGRATION.getFileName().toString());
    }

    @Test
    void the_seed_is_replayable() throws IOException {
        // It is loaded on every dev start-up, and V1 has already inserted the same rows in
        // any environment provisioned by Flyway.
        assertThat(Files.readString(SEED_MIGRATION))
                .as("the dev seed runs repeatedly, so every insert must guard against its own rows")
                .contains("WHERE NOT EXISTS");
    }

    /**
     * The code of every row a reply_medium insert declares - the first quoted
     * value of each tuple, whichever form the file uses: V1 lists them as
     * VALUES tuples, the dev seed as one guarded SELECT apiece.
     */
    private static Set<String> mediaIn(Path migration) throws IOException {
        Set<String> codes = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("(?:\\(|SELECT)\\s*'([A-Z_]+)'\\s*,");
        for (String statement : Files.readString(migration).split(";")) {
            if (!statement.contains("INSERT INTO reply_medium")) {
                continue;
            }
            Matcher matcher = pattern.matcher(statement);
            while (matcher.find()) {
                codes.add(matcher.group(1));
            }
        }
        if (codes.isEmpty()) {
            throw new IllegalStateException("No reply_medium insert found in " + migration.getFileName());
        }
        return codes;
    }

    private static Path resolveModuleRoot() {
        try {
            Path testClasses = Path.of(
                    ReplyMediumSeedTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // testClasses points at <module-root>/target/test-classes
            return testClasses.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not resolve module root for SQL seed scan", e);
        }
    }
}
