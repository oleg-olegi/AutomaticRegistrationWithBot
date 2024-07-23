package com.example.demo.registration.service;

import com.example.demo.model.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Log4j2
public class MessageGenerator {
    public String generateMessage(LocalDate localDate, String name) {
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        String dateFormat = localDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String messageTmp;
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            messageTmp = "\uD83C\uDF89%s\uD83C\uDF89\nУспешная регистрация на MZGB-Квиз!\nИгра состоится " +
                    "%s\n\uD83E\uDD18Rock&Rofl\uD83E\uDD18";
        } else {
            messageTmp = "\uD83C\uDF89%s\uD83C\uDF89\nУспешная регистрация на ТУЦ-Квиз!\nИгра состоится " +
                    "%s\n\uD83E\uDD18Rock&Rofl\uD83E\uDD18";
        }
        return String.format(messageTmp, name, dateFormat);
    }
}
