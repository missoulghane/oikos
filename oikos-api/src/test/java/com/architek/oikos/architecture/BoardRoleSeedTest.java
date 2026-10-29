package com.architek.oikos.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.user.application.port.out.BoardSeat;

/**
 * Guards the board_role catalog, seeded twice (V1 for prod, db/seed/ for the
 * dev profile - see {@link ReplyMediumSeedTest}), and its fit with the two
 * enums. Unlike reply media, rules branch on a seat: every {@link BoardRole}
 * needs a row (board_member and invitation reference it by foreign key), and
 * the seats a row offers at sign-up must be exactly those the sign-up endpoint
 * accepts ({@link BoardSeat}) - one more and the wizard offers a choice the API
 * refuses, one less and a seat the API accepts can no longer be picked.
 */
class BoardRoleSeedTest {

    private static final Path RESOURCES = resolveModuleRoot().resolve("src/main/resources");
    private static final Path CATALOG_MIGRATION = RESOURCES.resolve("db/migration/V1__baseline.sql");
    private static final Path SEED_MIGRATION = RESOURCES.resolve("db/seed/seed_board_role.sql");
    private static final Path DEV_PROFILE = RESOURCES.resolve("application-dev.yml");

    @Test
    void both_files_declare_the_same_catalog() throws IOException {
        assertThat(rolesIn(SEED_MIGRATION)).isEqualTo(rolesIn(CATALOG_MIGRATION));
    }

    @Test
    void every_board_role_has_a_row() throws IOException {
        assertThat(rolesIn(CATALOG_MIGRATION).keySet())
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(BoardRole.values()).map(Enum::name).toList());
    }

    @Test
    void the_seats_offered_at_signup_are_the_ones_the_signup_accepts() throws IOException {
        assertThat(rolesIn(CATALOG_MIGRATION).entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey))
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(BoardSeat.values()).map(Enum::name).toList());
    }

    @Test
    void the_dev_profile_loads_the_seed() throws IOException {
        assertThat(Files.readString(DEV_PROFILE)).contains(SEED_MIGRATION.getFileName().toString());
    }

    @Test
    void the_seed_is_replayable() throws IOException {
        assertThat(Files.readString(SEED_MIGRATION)).contains("WHERE NOT EXISTS");
    }

    /** code -> available_at_signup, for every row a board_role insert declares. */
    private static Map<String, Boolean> rolesIn(Path migration) throws IOException {
        Map<String, Boolean> roles = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("(?:\\(|SELECT)\\s*'([A-Z_]+)'\\s*,\\s*'[^']*'\\s*,\\s*\\d+\\s*,\\s*(true|false)");
        for (String statement : Files.readString(migration).split(";")) {
            if (!statement.contains("INSERT INTO board_role")) {
                continue;
            }
            Matcher matcher = pattern.matcher(statement);
            while (matcher.find()) {
                roles.put(matcher.group(1), Boolean.parseBoolean(matcher.group(2)));
            }
        }
        if (roles.isEmpty()) {
            throw new IllegalStateException("No board_role insert found in " + migration.getFileName());
        }
        return roles;
    }

    private static Path resolveModuleRoot() {
        try {
            Path testClasses = Path.of(
                    BoardRoleSeedTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return testClasses.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not resolve module root for SQL seed scan", e);
        }
    }
}
