package com.example.demo.registration.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InputPollOption;
import com.pengrad.telegrambot.request.PinChatMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPoll;
import com.pengrad.telegrambot.request.UnpinAllChatMessages;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AutomaticRegistrationServiceTest {
    @Mock
    private TelegramBot telegramBot;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageGenerator messageGenerator;
    @Spy
    @InjectMocks
    private AutomaticRegistrationService automaticRegistrationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
@Disabled
    @Test
    public void sendPollTest() {
        List<User> users = new ArrayList<>();

        User user1 = new User(1L, "Ivan", 1L, null);
        User user2 = new User(2L, "SRY", 2L, null);
        User user3 = new User(3L, "dgn", 3L, null);
        User user4 = new User(4L, "dngdnen", -11L, null);
        User user5 = new User(5L, "tne", -21L, null);

        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        users.add(user5);

        String question = "?";

        InputPollOption pollOption1 = new InputPollOption("Категорическое ДА");
        InputPollOption pollOption2 = new InputPollOption("Бессовестное НЕТ");
        InputPollOption pollOption3 = new InputPollOption("Робкое Отвечу позже");

        InputPollOption[] pollOptionsArray = {pollOption1, pollOption2, pollOption3};

        users.stream()
                .filter(user -> user.getChatId() < 0)
                .forEach(user -> {
                    assertTrue(user.getChatId() < 0);
                    SendPoll poll = new SendPoll(user.getChatId(), question, pollOptionsArray);
                    telegramBot.execute(poll);
                });


        verify(telegramBot, times(2)).execute(any(SendPoll.class));

        ArgumentCaptor<SendPoll> argumentCaptor = ArgumentCaptor.forClass(SendPoll.class);
        verify(telegramBot, times(2)).execute(argumentCaptor.capture());
        List<SendPoll> capturedPolls = argumentCaptor.getAllValues();

        assertEquals(2, capturedPolls.size());

        for (SendPoll poll : capturedPolls) {
            assertNotNull(poll);
        }
    }
@Disabled
    @Test
    void checkPinMsg() {
        Long chatId = -123456789L;
        LocalDate localDate = LocalDate.of(2024, 10, 15);

        // Мокируем поведение telegramBot.execute() для SendPoll
        SendResponse mockSendResponse = mock(SendResponse.class);
        Message mockMessage = mock(Message.class);
        when(mockMessage.messageId()).thenReturn(12345); // Пример messageId
        when(mockSendResponse.message()).thenReturn(mockMessage);
        when(telegramBot.execute(any(SendPoll.class))).thenReturn(mockSendResponse);

        // Выполняем метод sendPoll()
//        automaticRegistrationService.sendPoll(localDate, chatId);

        // Проверяем, что был вызван UnpinAllChatMessages и PinChatMessage
        verify(telegramBot).execute(any(UnpinAllChatMessages.class));
        verify(telegramBot).execute(any(PinChatMessage.class));
    }

//    @Test
//    void sendSuccessMessageTest() {
//        LocalDate localDate = LocalDate.now();
//
//        List<User> users = new ArrayList<>();
//
//        User user1 = new User(1L, "Ivan", 1L, null);
//        User user2 = new User(2L, "SRY", 2L, null);
//        User user3 = new User(3L, "dgn", 3L, null);
//        User user4 = new User(4L, "dngdnen", -11L, null);
//        User user5 = new User(5L, "tne", -21L, null);
//
//        users.add(user1);
//        users.add(user2);
//        users.add(user3);
//        users.add(user4);
//        users.add(user5);
//
//        String fridayMessage = "Friday";
//
//        when(userRepository.findAll()).thenReturn(users);
//        when(messageGenerator.generateMessage(localDate, user1.getName())).thenReturn(fridayMessage);
//
//        List<User> testUsers = userRepository.findAll();
//        assertEquals(5, testUsers.size());
//
//        for (User user : testUsers) {
//            telegramBot.execute(new SendMessage(user.getChatId(), fridayMessage));
//        }
//
//        verify(telegramBot, times(5)).execute(any(SendMessage.class));
//
//
//        automaticRegistrationService.sendSuccessMessage();
//
//        verify(automaticRegistrationService, times(1)).sendPoll(localDate, user4.getChatId());
//        verify(automaticRegistrationService, times(1)).sendPoll(localDate, user5.getChatId());
//        verify(automaticRegistrationService, times(0)).sendPoll(localDate, user1.getChatId());
//    }
}
