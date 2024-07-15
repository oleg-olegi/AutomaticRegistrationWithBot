package com.example.demo.configuration;

import com.pengrad.telegrambot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BotConfiguration {
    private final Logger logger = LoggerFactory.getLogger(BotConfiguration.class);
    @Value("${telegram.bot.token}")
    private String token;

    @Bean
    public TelegramBot telegramBot() {
        logger.info("Bean TelegramBot is created");
        return new TelegramBot(token);
    }
}