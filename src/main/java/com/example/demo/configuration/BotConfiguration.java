package com.example.demo.configuration;

import com.pengrad.telegrambot.TelegramBot;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BotConfiguration {
    @Value("${telegram.bot.token}")
    private String token;

    @Bean
    public TelegramBot telegramBot() {
        log.info("Bean TelegramBot is created");
        return new TelegramBot(token);
    }
}