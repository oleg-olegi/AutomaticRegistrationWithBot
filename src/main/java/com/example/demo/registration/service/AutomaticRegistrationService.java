package com.example.demo.registration.service;

import com.example.demo.model.User;
import com.example.demo.registration.Configuration;
import com.example.demo.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputPollOption;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPoll;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class AutomaticRegistrationService {
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebDriver driver;
    @Autowired
    private Configuration configuration;

    @Getter
    private boolean successRegistration = false;

    private boolean flag = true;
    private boolean buttonFlag = true;

    @Scheduled(cron = "00 59 11 ? * MON,FRI")
    public void scheduleTask() {
        try {
            performLogin();
            navigateToGameRegistrationPage();
            while (flag) {
                Thread.sleep(1000);
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

    private void performLogin() {
        driver.get("https://vtb.mzgb.net/account");
        log.info("Opened page of authorisation");

        try {
            WebElement emailInput = driver.findElement(By.name("email"));
            emailInput.sendKeys(Configuration.getEmail());
            log.info("Email entered.");

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.sendKeys(Configuration.getPassword());
            log.info("Password entered.");

            WebElement enterButton = driver.findElement(By.className("btn-filled"));
            enterButton.click();
            log.info("'Login' button pressed.");
        } catch (NoSuchElementException e) {
            log.error("Login element not found: {}", e.getMessage(), e);
            throw e;
        }
    }

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

    private void performRegistrationTask() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        if (12 == now.getHour() && 0 == now.getMinute()) {
            driver.navigate().refresh();
            log.info("Refreshed page");

            while (buttonFlag) {
                log.info("Cycle of checking the 'Зарегистрироваться' button");

                List<WebElement> registerButtons = driver.findElements(By.xpath(
                        "//button[contains(text(), 'Зарегистрироваться')]"));
                log.info("Count of buttons 'Зарегистрироваться' = {}", registerButtons.size());

                List<WebElement> activeButtons = registerButtons.stream()
                        .filter(button -> button.getAttribute("disabled") == null)
                        .toList();
                log.info("Count of active buttons 'Зарегистрироваться' = {}", activeButtons.size());

                if (activeButtons.isEmpty()) {
                    driver.navigate().refresh();
                    log.info("Page was refreshed in loop");
                    Thread.sleep(1500);
                    log.info("Waiting 1.5 sec");
                } else {
                    registerButtonClick(activeButtons.get(0));
                }
            }
            completeRegistrationSteps();
        } else {
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
            successRegistration = true;
            sendSuccessMessage();
            flag = false;
        } catch (NoSuchElementException e) {
            log.error("Error completing registration steps: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void clickMoveButton() {
        WebElement moveButton = driver.findElement(By.xpath("//button[contains(text(), 'Далее')]"));
        moveButton.click();
        log.info("Button 'Далее' was clicked");
    }

    private void clickPlusIconMultipleTimes(int times) {
        WebElement plusIcon = driver.findElement(By.cssSelector("img[src='/img/icons/plus.svg']"));
        for (int i = 0; i < times; i++) {
            plusIcon.click();
            log.info("Button '+' was clicked");
        }
    }

    private void clickFinalRegistrationButton() {
        WebElement registrationButton = driver.findElement(By.xpath("//button[contains(text(), 'Регистрация на игру')]"));
        registrationButton.click();
        log.info("Button 'Регистрация на игру' was clicked");
    }

    private void sendSuccessMessage() {
        log.info("In method sendSuccessMessage()");
        if (isSuccessRegistration()) {
            List<User> chatIdList = userRepository.findAll();
            LocalDate localDate = LocalDate.now();
            for (User user : chatIdList) {
                String message = String.format(
                        "\uD83C\uDF89%s\uD83C\uDF89\nУспешная регистрация на MZGB-Квиз!\nИгра состоится %s\n\uD83E\uDD18Rock&Rofl\uD83E\uDD18",
                        user.getName(),
                        localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                );
                telegramBot.execute(new SendMessage(user.getChatId(), message));
            }
            chatIdList.stream()
                    .filter(user -> user.getChatId() < 0)
                    .forEach(user -> sendPoll(localDate, user.getId()));
        }
    }

    private void sendPoll(LocalDate localDate, Long chatId) {
        String question;
        if (localDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            question = String.format("Иду на ТУЦ %s",
                    localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        } else {
            question = String.format("Иду на MZGB %s",
                    localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }
        InputPollOption pollOption1 = new InputPollOption("Категорически ДА");
        InputPollOption pollOption2 = new InputPollOption("Бессовестно НЕТ");
        InputPollOption pollOption3 = new InputPollOption("Робко Отвечу позже");

        InputPollOption[] pollOptionsArray = {pollOption1, pollOption2, pollOption3};

        SendPoll poll = new SendPoll(chatId, question, pollOptionsArray)
                .isAnonymous(false) // устанавливаем, будет ли опрос анонимным
                .allowsMultipleAnswers(false); // можно ли выбрать несколько ответов;
        telegramBot.execute(poll);
    }
}
