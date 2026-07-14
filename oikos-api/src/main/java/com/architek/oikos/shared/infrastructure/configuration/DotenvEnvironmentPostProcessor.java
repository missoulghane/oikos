package com.architek.oikos.shared.infrastructure.configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads a local {@code .env} file (relative to the JVM working directory) into the
 * Spring {@link org.springframework.core.env.Environment Environment} as the
 * lowest-priority property source, so real OS environment variables and
 * {@code -D} system properties always take precedence. A no-op when no
 * {@code .env} file is present, so it is harmless in CI/production, where
 * secrets are supplied as real environment variables.
 *
 * <p>Registered via {@code META-INF/spring.factories} rather than component
 * scanning or the {@code AutoConfiguration.imports} mechanism: {@link
 * EnvironmentPostProcessor} runs before the {@code ApplicationContext} exists,
 * so it can never be a managed Spring bean. Implements the Spring Boot 4
 * {@code org.springframework.boot.EnvironmentPostProcessor} (not the
 * {@code org.springframework.boot.env} variant, deprecated for removal since 4.0).
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";
    private static final Path DOTENV_PATH = Path.of(".env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Files.isRegularFile(DOTENV_PATH)) {
            return;
        }

        Map<String, Object> values = parse(DOTENV_PATH);
        if (!values.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
        }
    }

    private Map<String, Object> parse(Path path) {
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (String rawLine : Files.readAllLines(path)) {
                String line = rawLine.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }
                String key = line.substring(0, separatorIndex).strip();
                String value = unquote(line.substring(separatorIndex + 1).strip());
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read " + path.toAbsolutePath(), e);
        }
        return values;
    }

    private String unquote(String value) {
        boolean quoted = value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")));
        return quoted ? value.substring(1, value.length() - 1) : value;
    }
}
