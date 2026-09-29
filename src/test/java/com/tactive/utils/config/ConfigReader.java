package com.tactive.utils.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigReader {

    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure()
                    .directory("./env")       // Points to your 'env' folder
                    .filename(".env.stage")   // Points to your stage file
                    .load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load environment configuration file from env/.env.stage", e);
        }
    }

    /**
     * Generic getter to fetch any configuration value dynamically by its key name.
     * This fixes the compilation failures in your Page Objects and Step Definitions.
     *
     * @param key The environment variable key name (e.g., "TACTIVE_USERNAME")
     * @return The string value mapping to the key
     */
    public static String get(String key) {
        return dotenv.get(key);
    }

    /**
     * Retrieves the base URL for the Tactive application.
     */
    public static String getBaseUrl() {
        return dotenv.get("TACTIVE_BASE_URL");
    }

    /**
     * Retrieves the username for the Tactive application.
     */
    public static String getUsername() {
        return dotenv.get("TACTIVE_USERNAME");
    }

    /**
     * Retrieves the password for the Tactive application.
     */
    public static String getPassword() {
        return dotenv.get("TACTIVE_PASSWORD");
    }
}
