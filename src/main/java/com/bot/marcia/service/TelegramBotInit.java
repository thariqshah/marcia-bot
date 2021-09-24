package com.bot.marcia.service;

import com.bot.marcia.common.Util;
import com.bot.marcia.configuration.AppConfiguration;
import com.bot.marcia.service.impl.YtsLookupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Slf4j
@Service
public class TelegramBotInit extends TelegramLongPollingBot {

    @Autowired
    public YtsLookupService ytsLookupService;

    @Autowired
    public MovieInfoCreatorService movieInfoCreatorService;

    @Autowired
    public StringBuilderForTelegram stringBuilderForTelegram;

    @Autowired
    private AppConfiguration appConfiguration;

    @Override
    public String getBotToken() {
        return appConfiguration.getTelegramBotToken();
    }


    @Override
    public String getBotUsername() {
        return "marcia_movie_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.debug("message received via telegram from user the {}", update.getMessage().getFrom().getFirstName());
            this.reply(update);
        }
    }

    private void reply(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        if (!update.getMessage().isGroupMessage() && !update.getMessage().getFrom().getIsBot()) {
            replyToPrivateMessages(update, message);
        }
        else if (update.getMessage().isGroupMessage() && !update.getMessage().getFrom().getIsBot() && update.getMessage().getText().startsWith("@marcia_movie_bot") || update.getMessage().getText().startsWith("/start")) {
            replyToGroupMessages(update, message);
        }
        else if (update.getMessage().getReplyToMessage() !=null && update.getMessage().getReplyToMessage().getFrom().getUserName().equals("marcia_movie_bot")) {
            replyToPrivateMessages(update, message);
        }
        else return;
        try {
            setReplyToAMessageId(message,update.getMessage().getMessageId());
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void replyToPrivateMessages(Update update, SendMessage message) {
        if (update.getMessage().getText().equals("/start") || update.getMessage().getText().equals("/help") || update.getMessage().getText().equalsIgnoreCase("hello")) {
            message.setParseMode("HTML");
            message.setText(Util.buildTelegramIntroMessage(update.getMessage().getChat().getFirstName()));
        } else {
            this.lookupMovieSource(update, message);
        }
    }

    private void replyToGroupMessages(Update update, SendMessage message) {
        if (update.getMessage().getText().startsWith("/start")) {
            message.setParseMode("HTML");
            message.setText(Util.buildTelegramIntroMessage(update.getMessage().getFrom().getFirstName()));
        } else {
            this.lookupMovieSource(update, message);
        }
    }

    private void lookupMovieSource(Update update, SendMessage message) {
        try {
            if (update.getMessage().isGroupMessage() && update.getMessage().getText().startsWith("@marcia_movie_bot"))
                message.setText(stringBuilderForTelegram.buildMovieInfoTelegram(
                        movieInfoCreatorService.buildMovieInfo(
                                ytsLookupService.buildARequestWithQuery(update.getMessage().getText().substring(17)))));
            else
                message.setText(stringBuilderForTelegram.buildMovieInfoTelegram(
                        movieInfoCreatorService.buildMovieInfo(
                                ytsLookupService.buildARequestWithQuery(update.getMessage().getText()))));
            message.setParseMode("HTML");
            message.enableWebPagePreview();
        } catch (Exception e) {
            message.setText("I couldn't find what you are looking for \uD83D\uDC94");
            message.setParseMode("HTML");
            message.enableWebPagePreview();
        }
    }

    private SendMessage setReplyToAMessageId(SendMessage outgoingMessage,Integer incomingId){
        outgoingMessage.setReplyToMessageId(incomingId);
        return outgoingMessage;
    }

}
