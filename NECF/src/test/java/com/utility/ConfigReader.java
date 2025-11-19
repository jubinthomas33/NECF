package com.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
	private static Properties prop;

	public static void loadProperties() {
		prop = new Properties();
		try {
			FileInputStream fis = new FileInputStream("src/test/Resources/Config.properties");
			prop.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load config.properties file!");
		}

	}

	public static String getProperty(String key) {
		if (prop == null) {
			loadProperties();
		}
		return prop.getProperty(key);
	}
}
