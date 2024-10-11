package com.example.demo.registration.service;

import com.example.demo.model.User;
import com.example.demo.registration.Configuration;
import com.example.demo.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputPollOption;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPoll;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class AutomaticRegistrationService {
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebDriver driver;
    @Autowired
    private Configuration configuration;
    @Autowired
    private MessageGenerator messageGenerator;

    private boolean flag = true;
    private boolean buttonFlag = true;

    private static final int TARGET_HOUR = 12;
    private static final int TARGET_MINUTE = 0;
    private static final int MAX_COUNTER = 10;
    private static final int SLEEP_DURATION_MS_IN_LOOP = 1500;


    @Scheduled(cron = "00 59 11 ? * MON,FRI")
    public void scheduleTask() {
        log.info("Starting schedule task");
        try {
//1 - сначала проваливаемся в этот метод
            performLogin();
            log.info("Login successful");
//2 - потом сюда
            navigateToGameRegistrationPage();
            log.info("After method navigateToGameRegistrationPage");

            while (flag) {
                Thread.sleep(1000);
                log.info("Before method performRegistrationTask");
                // 3 - потом сюда
                performRegistrationTask();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Task interrupted: {}", e.getMessage(), e);
        } catch (WebDriverException e) {
            log.error("WebDriver error: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // 1
    private void performLogin() {
        // открываем сайт
        driver.get("https://vtb.mzgb.net/account");
        // браузер в полноэкранный режим
        driver.manage().window().maximize();
        log.info("Opened page of authorisation");

        try {
            //находим элемент для ввода мыла
            WebElement emailInput = driver.findElement(By.name("email"));
            //вводим мыло из конфиг файла
            emailInput.sendKeys(Configuration.getEmail());
            log.info("Email entered.");
            //находим элемент для ввода пароля
            WebElement passwordInput = driver.findElement(By.name("password"));
            // вводим пассворд
            passwordInput.sendKeys(Configuration.getPassword());
            log.info("Password entered.");
            // находим кнопку логин
            WebElement enterButton = driver.findElement(By.className("btn-filled"));
            //жмем
            enterButton.click();
            log.info("'Login' button pressed.");
        } catch (NoSuchElementException e) {
            log.error("Login element not found: {}", e.getMessage(), e);
            throw e;
        }
    }

    // выходим на главную страницу регистрации
    private void navigateToGameRegistrationPage() {
        try {
            WebElement element = driver.findElement(By.cssSelector(
                    "body > nav > div > div.flex.flex-row.items-center > div:nth-child(1) > a"));
            element.click();
            log.info("'Мозгобойня' element pressed");
        } catch (NoSuchElementException e) {
            log.error("Navigation element not found: {}", e.getMessage(), e);
            throw e;
        }
    }

    //3
    private void performRegistrationTask() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        if (now.getHour() == TARGET_HOUR && now.getMinute() == TARGET_MINUTE) {

            driver.navigate().refresh();
            log.info("Refreshed page");

            short counter = 0;

            while (buttonFlag) {

                log.info("Cycle of checking the 'Зарегистрироваться' button");

                List<WebElement> registerActiveButtons = driver.findElements(
                                By.xpath("//button[contains(text(), 'Зарегистрироваться')"))
                        .stream()
                        .filter(button -> button.getAttribute("disabled") == null)
                        .toList();

                log.info("Count of active buttons 'Зарегистрироваться' = {}", registerActiveButtons.size());

//                List<WebElement> activeButtons = registerButtons.stream()
//                        .filter(button -> button.getAttribute("disabled") == null)
//                        .toList();
//
//                log.info("Count of active buttons 'Зарегистрироваться' = {}", activeButtons.size());

                if (registerActiveButtons.isEmpty()) {
                    counter++;
                    driver.navigate().refresh();
                    log.info("Page was refreshed in loop");
                    Thread.sleep(SLEEP_DURATION_MS_IN_LOOP);
                    log.info("Waiting 1.5 sec");

                    // Здесь могут быть вопросы
                } else {
                    if (now.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                        log.info("Понедельник");
                        if (registerActiveButtons.size() == 1) {
                            log.info("Активных кнопок 1");
                            registerButtonClick(registerActiveButtons.get(0));
                        } else {

                            log.info("Активных кнопок 2, жмем вторую");
                            registerButtonClick(registerActiveButtons.get(1));
                        }
                } else{
                    registerButtonClick(registerActiveButtons.get(0));
                }
            }
            // до сюда
        }
        if (counter == MAX_COUNTER) {
            driver.quit();
        }
        completeRegistrationSteps();
    } else{
        log.info("It's not time for registration yet.");
    }
}

private void registerButtonClick(WebElement registerButton) {
    try {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView(true);", registerButton);
        registerButton.click();
        buttonFlag = false;
        log.info("Button 'Зарегистрироваться' was clicked");
    } catch (StaleElementReferenceException | NoSuchElementException e) {
        log.error("Error clicking register button: {}", e.getMessage(), e);
        throw e;
    }
}

private void completeRegistrationSteps() {
    try {
        clickMoveButton();
        clickPlusIconMultipleTimes(4);
        clickMoveButton();
        clickFinalRegistrationButton();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        log.info("Waiting 10 sec");
        sendSuccessMessage();
        flag = false;
    } catch (NoSuchElementException e) {
        log.error("Error completing registration steps: {}", e.getMessage(), e);
        throw e;
    }
}

private void clickMoveButton() {
    WebElement moveButton = driver.findElement(By.xpath("//button[contains(text(), 'Далее')]"));
    if (moveButton.isDisplayed() && moveButton.isEnabled()) {
        log.info("Кнопка Далее видна и доступна");
        moveButton.click();
    } else {
        log.info("Кнопка Далее НЕ видна и доступна");
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView(true);", moveButton);
        moveButton.click();
    }
    log.info("Button 'Далее' was clicked");
}

private void clickPlusIconMultipleTimes(int times) {
    WebElement plusIcon = driver.findElement(By.cssSelector("img[src='/img/icons/plus.svg']"));
    for (int i = 0; i < times; i++) {
        plusIcon.click();
        log.info("Button '+' was clicked {} times", times);
    }
    try {
        Thread.sleep(1500);
        log.info("Поток спит 1,5 сек");
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}

private void clickFinalRegistrationButton() {
    WebElement registrationButton = driver.findElement(By.xpath("//button[contains(text(), 'Регистрация на игру')]"));
    registrationButton.click();
    log.info("Button 'Регистрация на игру' was clicked");
}

public void sendSuccessMessage() {
    log.info("In method sendSuccessMessage()");

    List<User> chatIdList = userRepository.findAll();
    log.info("Number of chat ids is {}", chatIdList.size());

    LocalDate localDate = LocalDate.now();
    log.info("Local date is {}", localDate);

    log.info("Before loop to send success messages for users from list");
    for (User user : chatIdList) {
        String message = messageGenerator.generateMessage(localDate, user.getName());
        log.info("Message is {}", message);

        log.info("Trying to send success message");
        telegramBot.execute(new SendMessage(user.getChatId(), message));
    }
    log.info("Trying to send poll");
    log.info("chatIdList {}", chatIdList);
    chatIdList.stream()
            .filter(user -> user.getChatId() < 0)
            .forEach(user -> sendPoll(localDate, user.getChatId()));
}

public void sendPoll(LocalDate localDate, Long chatId) {
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

    InputPollOption pollOption1 = new InputPollOption("ПРИДУ");
    InputPollOption pollOption2 = new InputPollOption("ВСЕ СЛОЖНО");
    InputPollOption pollOption3 = new InputPollOption("В АКТИВНОМ ПОИСКЕ");

    InputPollOption[] pollOptionsArray = {pollOption1, pollOption2, pollOption3};

    SendPoll poll = new SendPoll(chatId, question, pollOptionsArray)
            .isAnonymous(false) // устанавливаем, будет ли опрос анонимным
            .allowsMultipleAnswers(false); // можно ли выбрать несколько ответов;

    log.info("Trying to do telegramBot.execute(poll)");
    telegramBot.execute(poll);
}
}
