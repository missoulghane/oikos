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

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;

/**
 * Guards the convocation channel catalog, which is seeded twice on purpose and
 * so can drift.
 *
 * <p>V1__baseline.sql creates the table and fills it, for prod and docker. The dev profile
 * cannot apply V1 - it runs on H2 with Flyway disabled and a schema generated
 * by Hibernate, so a migration dropping columns would fail on the first
 * statement - and loads db/seed/ as plain scripts through spring.sql.init instead.
 * Two files, one catalog: exactly the shape that made
 * {@link RolePermissionSeedTest} necessary for the RBAC bundles, one table
 * further along. A channel added to V1 alone works in prod and offers nothing
 * in dev.
 *
 * <p>The last assertion is the other half of the same problem: the two codes
 * the Java side names must exist as rows, or SendConvocationService refuses
 * every send with "no such channel".
 */
class ConvocationChannelSeedTest {

    private static final Path RESOURCES = resolveModuleRoot().resolve("src/main/resources");
    private static final Path CATALOG_MIGRATION = RESOURCES.resolve("db/migration/V1__baseline.sql");
    private static final Path SEED_MIGRATION = RESOURCES.resolve("db/seed/seed_convocation_channel.sql");
    private static final Path DEV_PROFILE = RESOURCES.resolve("application-dev.yml");

    @Test
    void both_files_declare_the_same_catalog() throws IOException {
        assertThat(channelsIn(SEED_MIGRATION))
                .as("db/seed/ re-seeds for dev what V1__baseline.sql seeds for prod - a channel in one and not the other is a "
                        + "select box that differs between environments")
                .containsExactlyInAnyOrderElementsOf(channelsIn(CATALOG_MIGRATION));
    }

    @Test
    void the_dev_profile_loads_the_seed() throws IOException {
        assertThat(Files.readString(DEV_PROFILE))
                .as("application-dev.yml must list the seed file in spring.sql.init.data-locations - without it the dev "
                        + "database has no channel at all and no convocation can be sent")
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

    @Test
    void every_code_the_application_names_exists_as_a_row() throws IOException {
        assertThat(channelsIn(CATALOG_MIGRATION))
                .as("ChannelCode names EMAIL and APP because an emitter implements them - a code with no "
                        + "catalog row makes every send fail with a 404 on the channel")
                .contains(ChannelCode.EMAIL.value(), ChannelCode.APP.value());
    }

    /**
     * The code of every row a convocation_channel insert declares - the first
     * quoted value of each tuple, whichever form the file uses: V1 lists them as
     * VALUES tuples, the dev seed as one guarded SELECT apiece.
     */
    private static Set<String> channelsIn(Path migration) throws IOException {
        Set<String> codes = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("(?:\\(|SELECT)\\s*'([A-Z_]+)'\\s*,");
        for (String statement : Files.readString(migration).split(";")) {
            if (!statement.contains("INSERT INTO convocation_channel")) {
                continue;
            }
            Matcher matcher = pattern.matcher(statement);
            while (matcher.find()) {
                codes.add(matcher.group(1));
            }
        }
        if (codes.isEmpty()) {
            throw new IllegalStateException("No convocation_channel insert found in " + migration.getFileName());
        }
        return codes;
    }

    private static Path resolveModuleRoot() {
        try {
            Path testClasses = Path.of(
                    ConvocationChannelSeedTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // testClasses points at <module-root>/target/test-classes
            return testClasses.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not resolve module root for SQL seed scan", e);
        }
    }
}
