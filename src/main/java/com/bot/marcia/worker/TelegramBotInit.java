package com.bot.marcia.worker;

import com.bot.marcia.common.Resources;
import com.bot.marcia.service.impl.YtsLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramBotInit extends TelegramLongPollingBot {

    @Autowired
    public YtsLookupService ytsLookupService;

    @Autowired
    public MovieInfoCreator movieInfoCreator;

    @Autowired
    public StringBuilderForTelegram stringBuilderForTelegram;


    @Override
    public String getBotToken() {
        return "1721139961:AAHJ8fc_ZMPhwQA2Jfu5euwyDoWQf5J9-IQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
            message.setChatId(update.getMessage().getChatId().toString());
            if (update.getMessage().getText().equals("/start") || update.getMessage().getText().equals("/help")||update.getMessage().getText().equalsIgnoreCase("hello")) {
                message.setParseMode("HTML");
                message.setText(Resources.buildTelegramIntroMessage(update.getMessage().getChat().getFirstName()));
            } else {
                try {
                    message.setText(stringBuilderForTelegram.buildMovieInfoTelegram(
                            movieInfoCreator.buildMovieInfo(
                                    ytsLookupService.buildARequestWithQuery(update.getMessage().getText()))));
                    message.setParseMode("HTML");
                    message.enableWebPagePreview();
                } catch (Exception e) {
                    message.setText("I couldn't find what you are looking for \uD83D\uDC94");
                    message.setParseMode("HTML");
                    message.enableWebPagePreview();
                }
            }
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "marcia_movie_bot";
    }
}
