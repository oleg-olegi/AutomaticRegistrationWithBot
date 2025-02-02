package com.example.demo.registration.service;

import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageGeneratorTest {
    private MessageGenerator messageGenerator;

    @BeforeEach
    void setUp() {
        messageGenerator = new MessageGenerator();
    }

    @Disabled
    @Test
    void testGenerateFridayMessage() {
        LocalDate localDate = LocalDate.of(2024, 7, 19);
        String expectedMessage = "🎉Ivan🎉\nУспешная регистрация на MZGB-Квиз!\nИгра состоится 22 июля 2024\n🤘Rock&Rofl🤘";

        User user = new User(1L, "Ivan", 1L, null);

        String actualMessage = messageGenerator.generateMessage(localDate, user.getName());

        assertEquals(expectedMessage, actualMessage);
    }

    @Disabled
    @Test
    void testGenerateMondayMessage() {
        LocalDate localDate = LocalDate.of(2024, 7, 22);
        String expectedMessage = "🎉Ivan🎉\nУспешная регистрация на ТУЦ-Квиз!\nИгра состоится 25 июля 2024\n🤘Rock&Rofl🤘";

        User user = new User(1L, "Ivan", 1L, null);

        String actualMessage = messageGenerator.generateMessage(localDate, user.getName());

        assertEquals(expectedMessage, actualMessage);
    }
}
