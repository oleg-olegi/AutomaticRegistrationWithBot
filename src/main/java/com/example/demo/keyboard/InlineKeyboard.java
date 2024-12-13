package com.example.demo.keyboard;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;

@Component
public class InlineKeyboard {

    public InlineKeyboardMarkup getButton() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton("Если хочешь, можно нажать")
                .callbackData("callback");
        inlineKeyboardMarkup.addRow(button);
        return inlineKeyboardMarkup;
    }
}
