package com.architek.oikos.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * ArchUnit operates on bytecode, but Lombok annotations use SOURCE retention and
 * leave no trace after compilation. This test enforces the "Lombok forbidden in
 * domain and application" rule (docs/ARCHITECTURE.md, rule 1) by scanning .java
 * source files directly.
 */
class LombokUsageSourceTest {

    private static final Path SOURCE_ROOT = resolveModuleRoot().resolve("src").resolve("main").resolve("java");

    @Test
    void domain_and_application_source_files_must_not_import_lombok() throws IOException {
        assertThat(scanForLombokUsage(SOURCE_ROOT))
                .as("domain/application source files must not import lombok")
                .isEmpty();
    }

    @Test
    void detects_lombok_import_in_domain_layer(@TempDir Path tempDir) throws IOException {
        Path offender = writeJavaFile(tempDir, "domain/Foo.java",
                "package foo.domain;\n\nimport lombok.Data;\n\n@Data\nclass Foo {}\n");

        assertThat(scanForLombokUsage(tempDir)).containsExactly(offender);
    }

    @Test
    void detects_lombok_static_import_and_fully_qualified_annotation(@TempDir Path tempDir) throws IOException {
        Path staticImport = writeJavaFile(tempDir, "application/Bar.java",
                "package foo.application;\n\nimport static lombok.AccessLevel.PRIVATE;\n");
        Path qualifiedUsage = writeJavaFile(tempDir, "domain/Baz.java",
                "package foo.domain;\n\n@lombok.Getter\nclass Baz {}\n");

        assertThat(scanForLombokUsage(tempDir)).containsExactlyInAnyOrder(staticImport, qualifiedUsage);
    }

    @Test
    void ignores_lombok_usage_outside_domain_and_application_layers(@TempDir Path tempDir) throws IOException {
        writeJavaFile(tempDir, "infrastructure/Qux.java",
                "package foo.infrastructure;\n\nimport lombok.Data;\n\n@Data\nclass Qux {}\n");

        assertThat(scanForLombokUsage(tempDir)).isEmpty();
    }

    private static Path writeJavaFile(Path root, String relativePath, String content) throws IOException {
        Path file = root.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return file;
    }

    private static List<Path> scanForLombokUsage(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(LombokUsageSourceTest::isInDomainOrApplicationLayer)
                    .filter(LombokUsageSourceTest::importsLombok)
                    .toList();
        }
    }

    private static boolean isInDomainOrApplicationLayer(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return normalized.contains("/domain/") || normalized.contains("/application/");
    }

    private static boolean importsLombok(Path path) {
        try {
            return Files.readString(path).lines()
                    .map(String::trim)
                    .anyMatch(line -> line.startsWith("import lombok.")
                            || line.startsWith("import static lombok.")
                            || line.contains("@lombok."));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Resolves the module root (the directory containing {@code src/}) from the
     * location of this test's compiled .class file rather than the JVM's working
     * directory, so the scan keeps working if this ever stops being run with the
     * module root as the working directory (e.g. a future parent aggregator POM).
     */
    private static Path resolveModuleRoot() {
        try {
            Path testClasses = Path.of(
                    LombokUsageSourceTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // testClasses points at <module-root>/target/test-classes
            return testClasses.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not resolve module root for source scan", e);
        }
    }
}
