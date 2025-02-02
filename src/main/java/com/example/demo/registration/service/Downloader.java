package com.example.demo.registration.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class Downloader {

    @Autowired
    private MessageGenerator messageGenerator;

    void downloadImages(WebDriver driverModel) {

        List<WebElement> cards = driverModel.findElements(By.className("card"));

        for (WebElement card : cards) {
            // Проверяем, есть ли в карточке текст "Туц-Туц"
            if (card.getText().contains("Туц Туц")) {
                // Ищем изображение с расширением .jpg
                WebElement imgElement = card.findElement(By.cssSelector("img[src$='.jpg']"));
                String     imgUrl     = imgElement.getAttribute("src");
                String savePath = "D:/IdeaProjects/GoPoints/gen3/platform/services/test/src/main/resources/images/quiz2.jpg";
                downloadImage(imgUrl, savePath);

                messageGenerator.findElements(card);
            }
        }
    }

    private void downloadImage(String imageUrl, String savePath) {
        try {
            URL           url        = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            if (!Files.exists(Path.of(savePath))) {
                File file = new File(savePath);
                file.createNewFile();
            }

            try (InputStream is = connection.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(is);
                 FileOutputStream fos = new FileOutputStream(savePath)) {

                byte[] buffer = new byte[8192]; // 8KB buffer
                int    bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}