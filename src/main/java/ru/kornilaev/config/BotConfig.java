package ru.kornilaev.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private final String name;
    private final String token;

    public BotConfig() {
        Properties prop = loadProperties();
        this.name = prop.getProperty("bot_name");
        this.token = prop.getProperty("token");
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    private Properties loadProperties() {
        Properties prop = new Properties();
        try (InputStream input = BotConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            prop.load(input);
            return prop;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration", e);
        }
    }
}
