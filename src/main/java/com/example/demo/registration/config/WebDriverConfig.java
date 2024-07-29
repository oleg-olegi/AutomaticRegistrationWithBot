package com.example.demo.registration.config;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class WebDriverConfig {
    @Bean
    public WebDriver webDriver() {
        log.info("Creating web driver");
        ChromeOptions options = new ChromeOptions();
        // Добавьте любые опции, которые вам нужны для конфигурации Chrome
        return new ChromeDriver(options);
    }
}
