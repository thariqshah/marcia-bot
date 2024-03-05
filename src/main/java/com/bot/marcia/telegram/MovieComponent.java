package com.bot.marcia.telegram;


import com.bot.marcia.dto.Result;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.bot.marcia.telegram.CallBackCommand.*;
import static com.bot.marcia.telegram.ParseMode.HTML;

@Component
public class MovieComponent {

    private final MessageTemplates messageTemplates;

    public MovieComponent(MessageTemplates messageTemplates) {
        this.messageTemplates = messageTemplates;
    }

    public SendMessage getMovieMessage(SendMessage message, Result movie, int index, List<InlineKeyboardButton> keyboardRow) {
        message.setText(messageTemplates.makePopularMovieHtml(movie, index));
        message.setParseMode(HTML.name());
        var download = InlineKeyboardButton.builder().text("Torrent 🏴‍☠️").callbackData(YTS_LOOKUP.name()).build();
        var buttons = new ArrayList<>(Collections.singletonList(download));
        buttons.addAll(keyboardRow);
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }
}
