package com.example.demo.registration.service;

import com.example.demo.model.User;
import com.example.demo.registration.Configuration;
import com.example.demo.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(AutomaticRegistrationService.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private UserRepository userRepository;


    private boolean flag = true;
    private boolean buttonFlag = true;

    @Getter
    private boolean successRegistration = false;///////////

    @Autowired
    private WebDriver driver;

    @Autowired
    private Configuration configuration;

    @Scheduled(cron = "50 59 11 * * MON") // Every Monday at 12:00
    public void scheduleTask() {
        // Открытие страницы для регистрации на игру
        driver.get("https://vtb.mzgb.net/account");
        logger.info("Opened page of authorisation");

        //ввод мыла
        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys(Configuration.getEmail());
        logger.info("Email entered.");

        //ввод пароля
        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(Configuration.getPassword());
        logger.info("Password entered.");

        //нажать Войти
        WebElement enterButton = driver.findElement(By.className("btn-filled"));
        enterButton.click();
        logger.info("'Login' button pressed.");

        // Находим элемент по CSS-селектору(мозгобойня)
        WebElement element = driver.findElement(By.cssSelector(
                "body > nav > div > div.flex.flex-row.items-center > div:nth-child(1) > a"));
        element.click();
        logger.info("'Мозгобойня' element pressed");

        while (flag) {
            // Периодическая проверка каждые 1 секунду
            try {
                Thread.sleep(1000); // Подождать 1 секунду
                performRegistrationTask(); // Выполнить задачу
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void performRegistrationTask() throws InterruptedException {
        // Создание объекта для текущей даты и времени
        LocalDateTime now = LocalDateTime.now();

        // Проверка, является ли сегодня понедельником и время 12.00
        if (DayOfWeek.MONDAY == now.getDayOfWeek() && 12 == now.getHour() && 0 == now.getMinute()) {
            driver.navigate().refresh();
            logger.info("Refreshed page");

            while (buttonFlag) {
                logger.info("Cycle of checking the 'Зарегистрироваться' button");
                List<WebElement> registerButtons = driver.findElements(By.xpath(
                        "//button[contains(text(), 'Зарегистрироваться')]"));
                logger.info("Count of buttons 'Зарегистрироваться' = {}", registerButtons.size());
                List<WebElement> activeButtons = registerButtons.stream()
                        .filter(button -> button.getAttribute("disabled") == null)
                        .toList();
                logger.info("Count of active buttons 'Зарегистрироваться' = {}", activeButtons.size());
                if (activeButtons.isEmpty()) {
                    driver.navigate().refresh();
                    logger.info("Page was refreshed in loop");
                    try {
                        Thread.sleep(1500); // Пауза на 1,5 секунды
                        logger.info("Waiting 1.5 sec");
                    } catch (InterruptedException e) {
                        // Обработка исключения
                        Thread.currentThread().interrupt();
                        logger.info("Пауза была прервана");
                    }
                    logger.info("List of active buttons is empty");
                } else {
                    registerButtonClick(activeButtons.get(0));
                    logger.info("Button 'Зарегистрироваться' was clicked");
                }
            }
            clickMoveButton();
//            WebElement moveButton = driver.findElement(By.xpath("//button[contains(text(), 'Далее')]"));
//            moveButton.click();
//            logger.info("Button 'Далее' was clicked");

            WebElement plusIcon = driver.findElement(By.cssSelector("img[src='/img/icons/plus.svg']"));
            for (int i = 0; i < 4; i++) {
                plusIcon.click();
                logger.info("Button '+' was clicked 4 times");
            }
            clickMoveButton();
//            WebElement moveButton1 = driver.findElement(By.xpath("//button[contains(text(), 'Далее')]"));
//            moveButton1.click();
//            logger.info("Button 'Далее' was clicked");

            WebElement registration = driver.findElement(By.xpath("//button[contains(text(), 'Регистрация на игру')]"));
            registration.click();
            logger.info("Button 'Регистрация на игру' was clicked");

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            logger.info("Waiting 10 sec");
            logger.info("Закрытие браузера.");
            driver.quit();
            successRegistration = true;
            sendSuccessMessage();
            flag = false;
        } else {
            logger.info("Еще не время для регистрации.");
        }
    }

    private void clickMoveButton() {
        WebElement moveButton1 = driver.findElement(By.xpath("//button[contains(text(), 'Далее')]"));
        moveButton1.click();
        logger.info("Button 'Далее' was clicked");
    }

    private void registerButtonClick(WebElement registerButton) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", registerButton);
            assert registerButton != null;
            registerButton.click();
            buttonFlag = false;
            logger.info("Кнопка 'Зарегистрироваться' нажата");
        } catch (StaleElementReferenceException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSuccessMessage() {
        log.info("В методе sendSuccessMessage()");
        if (isSuccessRegistration()) {
            List<User> chatIdList = userRepository.findAll();
            for (User user : chatIdList) {
                LocalDate localDate = LocalDate.now();
                String message = "\uD83C\uDF89" + user.getName() + "\uD83C\uDF89"
                        + "\nУспешная регистрация на Туц-Туц Квиз"
                        + "\nИгра состоится " + localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                        + "\n\uD83E\uDD18Rock&Rofl\uD83E\uDD18";
                telegramBot.execute(new SendMessage(user.getChatId(), message));
            }
        }
    }
}
