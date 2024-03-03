package com.bot.marcia.service;

import com.bot.marcia.common.Util;
import com.bot.marcia.service.impl.YtsLookupService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URL;
import java.util.ArrayList;


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

    @Value("${application-configurations.telegram-bot-token}")
    private String telegramBotToken;

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }


    @Override
    public String getBotUsername() {
        return "marcia_movie_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        var object = ytsLookupService.listMovies();
        if(update.hasCallbackQuery())
            this.answerCallBackQuery(update.getCallbackQuery());
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.debug("message received via telegram from user the {}", update.getMessage().getFrom().getFirstName());
            this.reply(update);
        }
    }

    @SneakyThrows
    private void answerCallBackQuery(CallbackQuery callbackQuery) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText("Sending you the torrent file!");
        answerCallbackQuery.setShowAlert(Boolean.TRUE);

        execute(answerCallbackQuery);
        this.sendFile( callbackQuery.getData(), String.valueOf(callbackQuery.getMessage().getChatId()), callbackQuery.getMessage().getMessageId());
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

    @SneakyThrows
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

           var movieInfo = update.getMessage().isGroupMessage() && update.getMessage().getText().startsWith("@marcia_movie_bot") ?
                    movieInfoCreatorService.buildMovieInfo(
                    ytsLookupService.buildARequestWithQuery(update.getMessage().getText().substring(17))) :  movieInfoCreatorService.buildMovieInfo(
                    ytsLookupService.buildARequestWithQuery(update.getMessage().getText()));

            if (update.getMessage().isGroupMessage() && update.getMessage().getText().startsWith("@marcia_movie_bot"))
                message.setText(stringBuilderForTelegram.buildMovieInfoTelegram(
                        movieInfoCreatorService.buildMovieInfo(
                                ytsLookupService.buildARequestWithQuery(update.getMessage().getText().substring(17)))));
            else
                message.setText(stringBuilderForTelegram.buildMovieInfoTelegram(
                        movieInfoCreatorService.buildMovieInfo(
                                ytsLookupService.buildARequestWithQuery(update.getMessage().getText()))));

            var buttons = new ArrayList<InlineKeyboardButton>();

            movieInfo.getTorrentUrl().forEach((key,value) -> buttons.add(InlineKeyboardButton.builder().text(key).callbackData(value.substring(32)).build()));

            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
            message.setReplyMarkup(inlineKeyboardMarkup);
            message.setParseMode("HTML");
            message.enableWebPagePreview();
        } catch (Exception e) {
            log.error("error looking up movie",e);
            message.setText("I couldn't find what you are looking for \uD83D\uDC94");
            message.setParseMode("HTML");
            message.enableWebPagePreview();
        }
    }

    private SendMessage setReplyToAMessageId(SendMessage outgoingMessage,Integer incomingId){
        outgoingMessage.setReplyToMessageId(incomingId);
        return outgoingMessage;
    }

    @SneakyThrows
    private void sendFile(String hash,String chatId,int messageId){
        SendDocument document = new SendDocument();
            document.setChatId(chatId);
            InputFile file = new InputFile();
            file.setMedia(new URL("https://yts.mx/torrent/download/%s".formatted(hash)).openStream(),hash+".torrent");
            document.setDocument(file);
            document.setReplyToMessageId(messageId);
            execute(document);
    }

}
