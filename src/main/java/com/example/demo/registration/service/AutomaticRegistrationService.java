package com.example.demo.registration.service;

import com.example.demo.model.User;
import com.example.demo.registration.Configuration;
import com.example.demo.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AutomaticRegistrationService {
    private static final int TARGET_HOUR = 12;
    private static final int TARGET_MINUTE = 0;
    private static final String SITE_URL = "https://vtb.mzgb.net/account";
    private static final int PUSH_PLUS_BUTTON_TIMES = 4;
    private static final LocalDate now = LocalDate.now();

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebDriver driver;
    @Autowired
    private Downloader imageDownloader;
    @Autowired
    PollSender pollSender;
    @Autowired
    MediaSender mediaSender;

    private boolean flag = true;

//    @Scheduled(cron = "30 59 11 ? * MON,FRI")
    @Scheduled(cron = "* 47 10 ? * MON,TUE,WED,FRI")
    public void scheduleTask() {
        log.info("Starting schedule task");
        try {
            //1 - сначала вводи логин и пароль
            performLogin();
            log.info("Login successful");
            // 3 потом регистрация
            navigateToGameRegistrationPage();
            log.info("After method navigateToGameRegistrationPage");
            Thread.sleep(2000);
            log.info("Before method performRegistrationTask");
            //2 - потом качаем картинку
            imageDownloader.downloadImages(driver);
            // 3 - потом сюда
            performRegistrationTask();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // 1
    private void performLogin() {
        // открываем сайт
        driver.get(SITE_URL);
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

    //2 выходим на главную страницу регистрации
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
        if (false) {
            log.info("Перед рекурсией");
            Thread.sleep(10000);
            performRegistrationTask();
        }
        if (true) {
            findTargetCardAndProcessRegistration();
        } else {
            System.exit(0);
        }
    }

    private void findTargetCardAndProcessRegistration() {
        List<WebElement> cards = driver.findElements(By.className("card"));
        Optional<WebElement> targetCard = cards.stream()
                .filter(card -> ( card.getText().contains("Туц Туц Квиз")) ||
                        (now.getDayOfWeek().equals(DayOfWeek.FRIDAY) && card.getText().contains("Мозгобойня")))
                .findFirst();
        if (targetCard.isPresent()) {
            log.info("КАРТОЧКА ДОСТУПНА");
            log.info(targetCard.get().getText());
            WebElement button = targetCard.get().findElement(By.xpath("//button[contains(@class, 'reg-event-btn')]"));
            if (button.getAttribute("disabled") == null) {
                registerButtonClick(button);
                completeRegistrationSteps();
            } else {
                driver.navigate().refresh();
                log.info("Refresh page in registration steps");
                findTargetCardAndProcessRegistration();
            }
        }
    }

    public void sendSuccessMessage() {
        log.info("In method sendSuccessMessage()");

        List<User> chatIdList = userRepository.findAll();
        log.info("Number of chat ids is {}", chatIdList.size());

        LocalDate localDate = LocalDate.now();
        log.info("Trying to send photo & poll");
        log.info("chatIdList {}", chatIdList);

        chatIdList.stream()
                .filter(user -> user.getChatId() < 0)
                .forEach(user -> {
                    try {
                        mediaSender.sendPhotoAndSendMessage(user.getChatId(), localDate, telegramBot);
                        pollSender.sendPoll(localDate, user.getChatId(), telegramBot);
                        mediaSender.sendVoice(user.getChatId(), telegramBot);
                    } catch (IOException e) {
                        telegramBot.execute(new SendMessage(user.getChatId(), "Не нашел нужную фотку (("));
                    }
                });
    }

    private void registerButtonClick(WebElement registerButton) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", registerButton);
            registerButton.click();
            log.info("Button 'Зарегистрироваться' was clicked");
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            log.error("Error clicking register button: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void completeRegistrationSteps() {
        try {
            clickMoveButton();
            clickPlusIconMultipleTimes(PUSH_PLUS_BUTTON_TIMES);
            clickMoveButton();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
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
            log.info("Button '+' was clicked {} times", i);
        }
        try {
            Thread.sleep(1500);
            log.info("Поток спит 1,5 сек");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void clickFinalRegistrationButton() {
        WebElement registrationButton = driver.findElement(By.className("reg-event-complete"));

        registrationButton.click();
        log.info("Button 'Регистрация на игру' was clicked");
    }
}
