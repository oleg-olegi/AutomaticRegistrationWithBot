package com.example.demo.registration.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputPollOption;
import com.pengrad.telegrambot.request.PinChatMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPoll;
import com.pengrad.telegrambot.request.UnpinAllChatMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class PollSender {

    static class Answers {
        private static String WILL_COME = "Приду";
        private static String WILL_NOT_COME = "Не приду";
        private static String ANSWEAR_LATER = "Позже отвечу";
        private static String IM_TIERED = "Я устал, я мухожук";
    }

    public void sendPoll(LocalDate localDate, Long chatId, TelegramBot telegramBot) {
        log.info("In method sendPoll()");
        String question;
        if (localDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            question = String.format("Иду на ТУЦ-ТУЦ\uD83C\uDFB6 %s",
                    localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            log.info("Question = {}", question);
        } else {
            question = String.format("Иду на МОЗГОБОЙНЮ\uD83E\uDDE0 %s",
                    localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            log.info("Question = {}", question);
        }

        InputPollOption pollOption1 = new InputPollOption(Answers.WILL_COME);
        InputPollOption pollOption2 = new InputPollOption(Answers.WILL_NOT_COME);
        InputPollOption pollOption3 = new InputPollOption(Answers.ANSWEAR_LATER);
        InputPollOption pollOption4 = new InputPollOption(Answers.IM_TIERED);

        InputPollOption[] pollOptionsArray = {pollOption1, pollOption2, pollOption3, pollOption4};

        SendPoll poll = new SendPoll(chatId, question, pollOptionsArray)
                .isAnonymous(false) // устанавливаем, будет ли опрос анонимным
                .allowsMultipleAnswers(false);
//                .replyMarkup(keyboard.getButton());// можно ли выбрать несколько ответов;
        //ставлю кнопку
//        poll.replyMarkup(keyboard.getButton());

        log.info("Trying to do telegramBot.execute(poll)");
        var pollMessage = telegramBot.execute(poll);
        if (pollMessage != null && pollMessage.message() != null) {
            Integer messageId = pollMessage.message().messageId();
            try {
                telegramBot.execute(new UnpinAllChatMessages(chatId)).description();
                telegramBot.execute(new PinChatMessage(chatId, messageId));
            } catch (Exception e) {
                telegramBot.execute(new SendMessage(chatId, "Произошла ошибка при попытке закрепить опрос"));
            }
        }
    }
}
