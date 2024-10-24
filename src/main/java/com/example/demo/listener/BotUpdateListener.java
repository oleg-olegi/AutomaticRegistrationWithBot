package com.example.demo.listener;

import com.example.demo.AutomaticRegistrationWithBotApplication;
import com.example.demo.model.User;
import com.example.demo.registration.service.AutomaticRegistrationService;
import com.example.demo.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InputPollOption;
import com.pengrad.telegrambot.request.SendPoll;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Log4j2
public class BotUpdateListener implements UpdatesListener {

    private final TelegramBot telegramBot;

    @Autowired
    public BotUpdateListener(TelegramBot telegramBot, UserRepository userRepository) {
        this.telegramBot = telegramBot;

    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
//            log.info("Processing update: {}", update);
            // Process your updates here
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
