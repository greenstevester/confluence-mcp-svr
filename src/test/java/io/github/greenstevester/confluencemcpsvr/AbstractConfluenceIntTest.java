package io.github.greenstevester.confluencemcpsvr;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

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

    private static final Map<String, String> envVariables = new java.util.HashMap<>();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        loadEnvironmentVariables();
        
        // Set properties dynamically instead of using System.setProperty
        registry.add("confluence.api.base-url", () -> 
            envVariables.getOrDefault("CONFLUENCE_API_BASE_URL", "http://localhost:8090"));
        registry.add("confluence.api.username", () -> 
            envVariables.getOrDefault("CONFLUENCE_API_USERNAME", "test-user"));
        registry.add("confluence.api.token", () -> 
            envVariables.getOrDefault("CONFLUENCE_API_TOKEN", "test-token"));
    }

    /**
     * Loads environment variables from .env file if it exists.
     * Maps environment variables to the envVariables map for test configuration.
     */
    protected static void loadEnvironmentVariables() {
        // First load from system environment variables
        String baseUrl = System.getenv("CONFLUENCE_API_BASE_URL");
        String username = System.getenv("CONFLUENCE_API_USERNAME");
        String token = System.getenv("CONFLUENCE_API_TOKEN");
        
        if (baseUrl != null) envVariables.put("CONFLUENCE_API_BASE_URL", baseUrl);
        if (username != null) envVariables.put("CONFLUENCE_API_USERNAME", username);
        if (token != null) envVariables.put("CONFLUENCE_API_TOKEN", token);
        
        // Then try to load from .env file (overrides system env vars)
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
                                
                                // Store in our map instead of System properties
                                envVariables.put(key, value);
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