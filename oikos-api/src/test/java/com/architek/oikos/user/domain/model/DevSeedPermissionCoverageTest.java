package com.architek.oikos.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Flyway is disabled on the dev profile, so db/dev/dev.sql hand-mirrors the
 * role_permission seed spread across the migrations. That mirroring has
 * already drifted once: V17 added 'messaging:broadcast' but dev.sql was not
 * updated, so every non-admin broadcast attempt returned 403 in dev while
 * working in prod. This pins the invariant that keeps the two in step -
 * ROLE_ADMIN bundles the full catalog, so a new Permission constant that
 * nobody seeded fails here instead of at runtime.
 */
class DevSeedPermissionCoverageTest {

    private static String devSeed() throws IOException {
        try (InputStream stream = DevSeedPermissionCoverageTest.class.getResourceAsStream("/db/dev/dev.sql")) {
            assertThat(stream).as("db/dev/dev.sql must be on the classpath").isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void the_dev_seed_grants_every_permission_of_the_catalog_to_role_admin() throws IOException {
        String seed = devSeed();

        List<Permission> notSeeded = Arrays.stream(Permission.values())
                .filter(permission -> !seed.contains("('ROLE_ADMIN', '" + permission.key() + "')"))
                .toList();

        assertThat(notSeeded)
                .as("permissions missing from ROLE_ADMIN in db/dev/dev.sql - add them there too")
                .isEmpty();
    }
}
