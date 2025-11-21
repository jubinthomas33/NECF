package com.utility;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties prop;

    public static void loadProperties() {
        try {
            prop = new Properties();
            InputStream is = ConfigReader.class.getClassLoader()
                    .getResourceAsStream("Config.properties");

            if (is == null) {
                throw new RuntimeException("config.properties NOT FOUND in classpath!");
            }

            prop.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties!", e);
        }
    }

    public static String getProperty(String key) {
        if (prop == null) {
            loadProperties();
        }
        return prop.getProperty(key);
    }
}
