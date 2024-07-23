package com.example.demo.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {
    @Bean
    public WebDriver webDriver() {
        ChromeOptions options = new ChromeOptions();
        // Добавьте любые опции, которые вам нужны для конфигурации Chrome
        return new ChromeDriver(options);
    }
}
