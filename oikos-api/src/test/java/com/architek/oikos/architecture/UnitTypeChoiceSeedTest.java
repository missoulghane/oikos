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
 * Guards the unit_type_choice catalog, seeded twice on purpose (see
 * {@link ReplyMediumSeedTest}): a type in V1 and not in db/seed/ is a sign-up
 * wizard that differs between dev and prod.
 */
class UnitTypeChoiceSeedTest {

    private static final Path RESOURCES = resolveModuleRoot().resolve("src/main/resources");
    private static final Path CATALOG_MIGRATION = RESOURCES.resolve("db/migration/V1__baseline.sql");
    private static final Path SEED_MIGRATION = RESOURCES.resolve("db/seed/seed_unit_type_choice.sql");
    private static final Path DEV_PROFILE = RESOURCES.resolve("application-dev.yml");

    @Test
    void both_files_declare_the_same_catalog() throws IOException {
        assertThat(rowsIn(SEED_MIGRATION)).containsExactlyInAnyOrderElementsOf(rowsIn(CATALOG_MIGRATION));
    }

    @Test
    void the_dev_profile_loads_the_seed() throws IOException {
        assertThat(Files.readString(DEV_PROFILE)).contains(SEED_MIGRATION.getFileName().toString());
    }

    @Test
    void the_seed_is_replayable() throws IOException {
        assertThat(Files.readString(SEED_MIGRATION)).contains("WHERE NOT EXISTS");
    }

    /** Each row whole (name, plural, description, position): a description edited in one file only is drift too. */
    private static Set<String> rowsIn(Path migration) throws IOException {
        Set<String> rows = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("(?:\\(|SELECT)\\s*('[^']*'\\s*,\\s*'[^']*'\\s*,\\s*'[^']*'\\s*,\\s*\\d+)");
        for (String statement : Files.readString(migration).split(";")) {
            if (!statement.contains("INSERT INTO unit_type_choice")) {
                continue;
            }
            Matcher matcher = pattern.matcher(statement);
            while (matcher.find()) {
                rows.add(matcher.group(1).replaceAll("\\s+", " "));
            }
        }
        if (rows.isEmpty()) {
            throw new IllegalStateException("No unit_type_choice insert found in " + migration.getFileName());
        }
        return rows;
    }

    private static Path resolveModuleRoot() {
        try {
            Path testClasses = Path.of(
                    UnitTypeChoiceSeedTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return testClasses.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not resolve module root for SQL seed scan", e);
        }
    }
}
