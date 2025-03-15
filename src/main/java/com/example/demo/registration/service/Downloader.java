package com.example.demo.registration.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
public class Downloader {

    @Autowired
    private MessageGenerator messageGenerator;
    private final LocalDate now = LocalDate.now();

    void downloadImages(WebDriver driverModel) {

        List<WebElement> cards = driverModel.findElements(By.className("card"));

        cards.stream()
                .filter(card -> (now.getDayOfWeek().equals(DayOfWeek.MONDAY) && card.getText().contains("Туц Туц")) ||
                        (now.getDayOfWeek().equals(DayOfWeek.FRIDAY) && card.getText().contains("Мозгобойня")))
                .findFirst() // Находим первый подходящий элемент
                .ifPresent(card -> {  // Проверяем, найден ли элемент
                    findAndDownloadImg(card);
                    messageGenerator.findElements(card);
                });
                }

    private void findAndDownloadImg(WebElement card) {
        WebElement imgElement = card.findElement(By.cssSelector("img[src$='.jpg']"));
        String imgUrl = imgElement.getAttribute("src");
        String savePath = "C:/Users/trash/IdeaProjects/AutomaticRegistrationWithBot/images/quiz2.jpg"; // Путь к файлу

        // Перезаписываем файл
        downloadImage(imgUrl, savePath);
    }

    private void downloadImage(String imageUrl, String savePath) {
        try {
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            // Удаляем файл, если он существует
            File file = new File(savePath);
            if (file.exists()) {
                file.delete(); // Удаляем старый файл, чтобы перезаписать его
            }

            // Создаем новый файл
            file.createNewFile();

            try (InputStream is = connection.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(is);
                 FileOutputStream fos = new FileOutputStream(savePath)) {

                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
