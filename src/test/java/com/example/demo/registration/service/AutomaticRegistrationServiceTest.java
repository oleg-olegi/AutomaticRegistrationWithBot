package com.example.demo.registration;

import com.example.demo.registration.service.AutomaticRegistrationService;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputPollOption;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPoll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AutomaticRegistrationServiceTest {
    @Mock
    private TelegramBot telegramBot;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AutomaticRegistrationService automaticRegistrationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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

    @Test
    void sendSuccessMessageTest() {
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

        DayOfWeek friday = DayOfWeek.FRIDAY;
        DayOfWeek monday = DayOfWeek.MONDAY;

        String fridayMessage = "Friday";
        String mondayMessage = "Monday";

        when(userRepository.findAll()).thenReturn(users);

        List<User> testUsers = userRepository.findAll();
        assertEquals(5, testUsers.size());


        for (User user : testUsers) {
            telegramBot.execute(new SendMessage(user.getChatId(), fridayMessage));
        }

        verify(telegramBot, times(5)).execute(any(SendMessage.class));
    }
}