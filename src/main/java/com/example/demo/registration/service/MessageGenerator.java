package com.example.demo.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class MessageGenerator {
    public String generateMessage(LocalDate localDate, String name) {
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        String dateFormat = localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String messageTmp;
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            messageTmp = "\uD83C\uDF89%s\uD83C\uDF89\nЭТО регистрация на МОЗГОБОЙНЮ!\uD83E\uDDE0\nИгра в ПОНЕДЕЛЬНИК " +
                    "%s\n\uD83E\uDD18Rock&Rofl\uD83E\uDD18";
        } else {
            messageTmp = "\uD83C\uDF89%s\uD83C\uDF89\nЭТО регистрация на ТУЦ-ТУЦ!\uD83C\uDFB6\nИгра в ЧЕТВЕРГ " +
                    "%s\n\uD83E\uDD18Rock&Rofl\uD83E\uDD18";
        }
        return String.format(messageTmp, name, dateFormat);
    }
}
