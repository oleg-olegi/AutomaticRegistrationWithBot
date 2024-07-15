package com.example.demo.registration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Component
public class Configuration {
    private static final String CONFIG_FILE = "C:/Users/trash/IdeaProjects/AutomaticRegistrationWithBot/src" +
            "/main/resources/application.properties";
    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(CONFIG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getEmail() {
        return properties.getProperty("email");
    }

    public static String getPassword() {
        return properties.getProperty("password");
    }
}
