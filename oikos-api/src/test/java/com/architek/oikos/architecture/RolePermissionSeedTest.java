package com.architek.oikos.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.architek.oikos.user.domain.model.Permission;

/**
 * Guards the RBAC seed data, which has now been lost or gone stale three times.
 *
 * The `permission` catalog and the role -> permission bundles live in SQL, the
 * keys they reference live in the {@link Permission} enum, and nothing at
 * compile time ties the three together: a permission added to the enum but not
 * to the catalog, or a bundle referencing a key that no longer exists, both
 * compile perfectly and fail only at runtime - as a 403 nobody can explain, or
 * as a foreign-key violation during a production migration.
 *
 * These assertions run on the SQL sources themselves (like LombokUsageSourceTest
 * scans .java sources) so the build fails before such a change can be merged.
 */
class RolePermissionSeedTest {

    private static final Path RESOURCES = resolveModuleRoot().resolve("src/main/resources");
    private static final Path MIGRATIONS = RESOURCES.resolve("db/migration");
    private static final Path SEEDS = RESOURCES.resolve("db/seed");
    private static final Path DEV_SEED = RESOURCES.resolve("db/dev/dev.sql");
    private static final Path DEV_PROFILE = RESOURCES.resolve("application-dev.yml");

    /**
     * Read across db/migration AND db/seed. Since the 2026-08-17 squash there is exactly one
     * migration - V1__baseline.sql, carrying the catalog and the bundles for a real deployment -
     * and db/seed/ carries the same rows for the dev profile, which cannot run V1 at all (H2,
     * Flyway disabled). Scanning both is what keeps a key added to one and not the other from
     * compiling perfectly and failing as a 403 in one environment only.
     */
    private static final String CATALOG_INSERT = "INSERT INTO permission (key, description) VALUES";
    private static final String BUNDLE_INSERT = "INSERT INTO role_permission (role_name, permission_key)";

    @Test
    void permission_catalog_matches_the_Permission_enum_exactly() throws IOException {
        Set<String> catalog = catalogKeys();
        Set<String> enumKeys = new LinkedHashSet<>();
        for (Permission permission : Permission.values()) {
            enumKeys.add(permission.key());
        }

        assertThat(catalog)
                .as("the `permission` catalog seeded in V1 and the Permission enum must declare the same keys - "
                        + "a key in one and not the other is a 403 at runtime or a FK violation at migration time")
                .containsExactlyInAnyOrderElementsOf(enumKeys);
    }

    @Test
    void every_bundled_permission_exists_in_the_catalog() throws IOException {
        assertThat(bundledKeys())
                .as("every permission_key granted by V2 must exist in the `permission` catalog it FKs to")
                .isSubsetOf(catalogKeys());
    }

    @Test
    void every_catalogued_permission_is_granted_to_at_least_one_role() throws IOException {
        assertThat(catalogKeys())
                .as("a permission nobody holds is dead weight - grant it or drop it from the catalog")
                .isSubsetOf(bundledKeys());
    }

    /**
     * Carried over from DevSeedPermissionCoverageTest, which asserted this against
     * db/dev/dev.sql back when that file kept its own copy of the bundles. Its
     * incident is worth keeping: V17 added 'messaging:broadcast' and the seed was
     * not updated, so every non-admin broadcast returned 403 in dev while working
     * in prod. Anchoring on ROLE_ADMIN is what catches a brand-new Permission
     * constant that nobody granted - the weaker "some role holds it" above would
     * pass as soon as any single role did.
     */
    @Test
    void role_admin_bundles_the_entire_catalog() throws IOException {
        assertThat(keysGrantedTo("ROLE_ADMIN"))
                .as("ROLE_ADMIN must bundle every permission of the catalog - a new Permission constant "
                        + "granted to nobody has to fail here rather than as a 403 at runtime")
                .containsExactlyInAnyOrderElementsOf(catalogKeys());
    }

    private static Set<String> keysGrantedTo(String roleName) throws IOException {
        return matchesIn(bundleStatements(), "\\('" + roleName + "',\\s*'([^']+)'\\)");
    }

    /**
     * dev.sql used to keep its own copy of the bundles, "kept in sync manually".
     * That is the sync that drifted; the dev profile now loads V2 itself through
     * spring.sql.init. Re-introducing a copy here would also break the docker
     * profile, which applies V2 via Flyway *and* runs dev.sql.
     */
    @Test
    void dev_seed_does_not_declare_its_own_bundles() throws IOException {
        assertThat(Files.readString(DEV_SEED))
                .as("db/dev/dev.sql must not seed role_permission - the migrations are the single source")
                .doesNotContain("INSERT INTO role_permission");
    }

    /**
     * The dev profile runs on H2 with Flyway disabled: it never applies a migration, it loads the
     * bundle migrations as plain scripts through spring.sql.init. A module shipping its own bundles
     * in a new migration (V5 for `meeting`) and forgetting this list authorizes nothing in dev
     * while working in prod - the exact shape of the incident this class was written after, one
     * file further along.
     */
    @Test
    void every_seed_file_is_loaded_by_the_dev_profile() throws IOException {
        // V1 is deliberately not in that list and cannot be: the dev profile runs on H2 with
        // Flyway disabled and a Hibernate-generated schema, so it never applies a migration -
        // it loads db/seed/ as plain scripts. A seed file that exists but is not listed
        // authorizes nothing in dev while working in production, which is the exact shape of
        // the incident this class was written after.
        String devProfile = Files.readString(DEV_PROFILE);
        for (Path seed : sqlFilesIn(SEEDS)) {
            assertThat(devProfile)
                    .as("%s must be listed in application-dev.yml spring.sql.init.data-locations - "
                            + "otherwise its rows exist in production only", seed.getFileName())
                    .contains(seed.getFileName().toString());
        }
    }

    @Test
    void the_bundles_shipped_to_production_and_to_dev_are_the_same() throws IOException {
        // The squash left the same rows in two places on purpose - V1 for a real deployment,
        // db/seed/ for dev - and duplication that nothing checks is duplication that drifts.
        assertThat(bundleKeysIn(sqlFilesIn(SEEDS)))
                .as("the role -> permission bundles in db/seed/ must match those in V1__baseline.sql - "
                        + "a grant in one and not the other is an authorization that differs by environment")
                .containsExactlyInAnyOrderElementsOf(bundleKeysIn(sqlFilesIn(MIGRATIONS)));
    }


    private static Set<String> catalogKeys() throws IOException {
        return matchesIn(statementsWithPrefix(CATALOG_INSERT), "\\('([^']+)',");
    }

    private static Set<String> bundledKeys() throws IOException {
        return matchesIn(bundleStatements(), "\\('[A-Z_]+',\\s*'([^']+)'\\)");
    }

    private static List<String> bundleStatements() throws IOException {
        return statementsWithPrefix(BUNDLE_INSERT);
    }

    /** The (role, permission) pairs a given set of SQL files grants. */
    private static Set<String> bundleKeysIn(List<Path> files) throws IOException {
        Set<String> pairs = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("\\('([A-Z_]+),?\\s*'?|\\('([A-Z_]+)',\\s*'([^']+)'\\)");
        for (Path file : files) {
            String sql = Files.readString(file);
            int start = sql.indexOf(BUNDLE_INSERT);
            while (start >= 0) {
                int end = sql.indexOf(';', start);
                String statement = end < 0 ? sql.substring(start) : sql.substring(start, end);
                Matcher matcher = Pattern.compile("\\('([A-Z_]+)',\\s*'([^']+)'\\)").matcher(statement);
                while (matcher.find()) {
                    pairs.add(matcher.group(1) + " -> " + matcher.group(2));
                }
                start = sql.indexOf(BUNDLE_INSERT, start + BUNDLE_INSERT.length());
            }
        }
        return pairs;
    }

    /**
     * Every SQL statement introduced by the given prefix, across every migration, each cut at
     * its terminating semicolon. Scoping the tuple regexes to these statements - rather than
     * running them over whole files - is what keeps an unrelated multi-column INSERT elsewhere
     * in a migration (the `journal` reference table, say) from being read as RBAC seed data.
     */
    private static List<String> statementsWithPrefix(String prefix) throws IOException {
        List<String> statements = new ArrayList<>();
        for (Path migration : migrationFiles()) {
            String sql = Files.readString(migration);
            int start = sql.indexOf(prefix);
            while (start >= 0) {
                int end = sql.indexOf(';', start);
                statements.add(end < 0 ? sql.substring(start) : sql.substring(start, end));
                start = sql.indexOf(prefix, start + prefix.length());
            }
        }
        if (statements.isEmpty()) {
            throw new IllegalStateException("No migration carries a statement starting with: " + prefix);
        }
        return statements;
    }

    /** Both sources of seed SQL: the deployment's single migration, and the dev profile's copies. */
    private static List<Path> migrationFiles() throws IOException {
        List<Path> files = new ArrayList<>(sqlFilesIn(MIGRATIONS));
        files.addAll(sqlFilesIn(SEEDS));
        return files;
    }

    private static List<Path> sqlFilesIn(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".sql")).sorted().toList();
        }
    }

    private static Set<String> matchesIn(List<String> statements, String regex) {
        Set<String> keys = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile(regex);
        for (String statement : statements) {
            Matcher matcher = pattern.matcher(statement);
            while (matcher.find()) {
                keys.add(matcher.group(1));
            }
        }
        return keys;
    }

    private static Path resolveModuleRoot() {
        try {
            Path testClasses = Path.of(
                    RolePermissionSeedTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // testClasses points at <module-root>/target/test-classes
            return testClasses.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not resolve module root for SQL seed scan", e);
        }
    }
}
