package com.example.demo.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class MessageGenerator {

    private static String TIME;
    private static String PLACE;
    private static String ADRESS;
    private static String PRICE;

    public void findElements(WebElement card) {

        WebElement timeElement = card.findElement(By.cssSelector("div.flex.items-center p"));
        TIME = timeElement.getText();
        WebElement placeElement = card.findElement(By.cssSelector("div.flex.items-center.mt-3.relative p.venue"));
        PLACE = placeElement.getText();
        List<WebElement> venueElements = card.findElements(By.cssSelector("div.flex.items-center.mt-3.relative p.venue"));
        if (venueElements.size() > 1) {
            ADRESS = venueElements.get(1).getText();  // Safely access the second element
        }
        WebElement costElement = card.findElement(By.cssSelector("div.flex.items-center.mt-3 p.font-bold"));
        PRICE = costElement.getText();
    }


    public String generateMessage(LocalDate localDate) {
        DayOfWeek dayOfWeek  = localDate.getDayOfWeek();
        String    dateFormat = localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String    messageTmp;
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            messageTmp = "\uD83C\uDF89ЭТО регистрация на МОЗГОБОЙНЮ!\uD83C\uDF89\uD83E\uDDE0\nИгра в ПОНЕДЕЛЬНИК " +
                         "%s\n\uD83E\uDD18Rock&Rofl\uD83E\uDD18";
        } else {
            messageTmp = """
                    \uD83C\uDF89МЫ идем на ТУЦ-ТУЦ!\uD83C\uDF89\uD83C\uDFB6
                    КОГДА? - %s
                    ВО СКОЛЬКО? - %s
                    ГДЕ? - %s
                    АДРЕС? - %s
                    ЦЕНА? - %s
                    \n\uD83E\uDD18Rock&Rofl\uD83E\uDD18
                    """;
        }
        return String.format(messageTmp, dateFormat, TIME, PLACE, ADRESS, PRICE);
    }
}
