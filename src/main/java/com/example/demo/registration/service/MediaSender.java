package com.example.demo.registration.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaSender {
    @Autowired
    MessageGenerator messageGenerator;

    public void sendPhotoAndSendMessage(Long chatId, LocalDate localDate, TelegramBot telegramBot) throws IOException {
        log.info("PHOTO METHOD!!!");
        String imagePath = "C:/Users/trash/IdeaProjects/AutomaticRegistrationWithBot/images/quiz2.jpg"; // Или из конфигурации: @Value("${my.file.path}") String imagePath;
        File imgFile = new File(imagePath);  // Используем File для работы с файловой системой
        try (InputStream stream = new FileInputStream(imgFile)) {
            byte[] imageBytes = stream.readAllBytes();
            SendPhoto sendPhoto = new SendPhoto(chatId, imageBytes).caption(messageGenerator.generateMessage(localDate));
            telegramBot.execute(sendPhoto);
        } catch (IOException e) {
            log.error("Error while sending photo", e);
            throw e;
        }
    }

    public void sendVoice(Long chatId, TelegramBot telegramBot) {
        String path = "C:/Users/trash/IdeaProjects/AutomaticRegistrationWithBot/audio/pila.ogg";
        File audioFile = new File(path);
        try (InputStream stream = new FileInputStream(audioFile)) {
            byte[] bytes = stream.readAllBytes();
            SendVoice sendVoice = new SendVoice(chatId, bytes);
            telegramBot.execute(sendVoice);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
