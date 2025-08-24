package io.github.greenstevester.confluencemcpsvr;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Abstract base class for Confluence integration tests.
 * Provides common functionality for loading environment variables and test configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractConfluenceIntTest {

    private static final Map<String, String> ENV_TO_PROPERTY_MAP = Map.of(
            "CONFLUENCE_API_BASE_URL", "confluence.api.base-url",
            "CONFLUENCE_API_USERNAME", "confluence.api.username",
            "CONFLUENCE_API_TOKEN", "confluence.api.token"
    );

    @BeforeAll
    static void setupEnvironment() {
        loadEnvironmentVariables();
    }

    /**
     * Loads environment variables from .env file if it exists.
     * Maps environment variables to Spring properties for test configuration.
     */
    protected static void loadEnvironmentVariables() {
        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            try {
                Files.lines(envFile)
                        .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                        .forEach(line -> {
                            String[] parts = line.split("=", 2);
                            if (parts.length == 2) {
                                String key = parts[0].trim();
                                String value = parts[1].trim();
                                
                                // Remove surrounding quotes if present
                                if (value.startsWith("\"") && value.endsWith("\"")) {
                                    value = value.substring(1, value.length() - 1);
                                }
                                
                                System.setProperty(key, value);
                                
                                // Map environment variable to Spring property
                                String mappedProperty = ENV_TO_PROPERTY_MAP.get(key);
                                if (mappedProperty != null) {
                                    System.setProperty(mappedProperty, value);
                                }
                            }
                        });
                System.out.println("Successfully loaded environment variables from .env file");
            } catch (IOException e) {
                System.err.println("Warning: Could not load .env file: " + e.getMessage());
            }
        } else {
            System.out.println("No .env file found, using system environment variables");
        }
    }
}