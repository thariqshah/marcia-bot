package com.bot.marcia.telegram;

import com.bot.marcia.common.Util;
import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.moviedb.MovieDBClient;
import com.bot.marcia.service.MovieInfoCreatorService;
import com.bot.marcia.service.StringBuilderForTelegram;
import com.bot.marcia.service.impl.YtsLookupService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.net.URL;
import java.util.ArrayList;

@RequiredArgsConstructor
@Slf4j
@Component
public class MarciaBot extends TelegramLongPollingBot {

    public final YtsLookupService ytsLookupService;

    public final MovieInfoCreatorService movieInfoCreatorService;

    public final StringBuilderForTelegram stringBuilderForTelegram;

    private final MovieDBClient movieDBClient;

    @Value("${application-configurations.telegram-bot-token}")
    private String telegramBotToken;

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    private final MessageTemplates messageTemplates;

    @Override
    public String getBotUsername() {
        return "marcia_movie_bot";
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("NEXT_POPULAR_"))
            this.answerCallBackQueryForNextPopular(update.getCallbackQuery());
        else if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("YTS_LOOKUP"))
            this.answerCallForYtsLookup(update);
        else if (update.hasCallbackQuery())
            this.answerCallBackQuery(update.getCallbackQuery());
        else if (update.getMessage().getText().startsWith("/wheretowatch")) {
            this.answerWhereToWatch(update);
        } else if (update.getMessage().getText().startsWith("/download")) {
            log.debug("message received via telegram from user the {}", update.getMessage().getFrom().getFirstName());
            try {
                update.getMessage().setText(update.getMessage().getReplyToMessage().getEntities().get(2).getText());
                this.reply(update);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (update.getMessage().getText().startsWith("")) {
            try {
                this.findAMovie(update);
            } catch (Exception e) {
                SendMessage message = new SendMessage();
                log.error("error looking up movie", e);
                message.setReplyToMessageId(update.getMessage().getMessageId());
                message.setChatId(update.getMessage().getChatId());
                message.setText("I couldn't find what you are looking for \uD83D\uDC94");
                message.setParseMode("HTML");
                message.enableWebPagePreview();
                execute(message);
            }
        }
    }

    private void answerCallForYtsLookup(Update update) {
        try {
            SendMessage message = new SendMessage();
            var some = (Message) update.getCallbackQuery().getMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
            message.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
            message.setParseMode("HTML");
            message.enableWebPagePreview();
            some.setText(some.getEntities().get(2).getText());
            update.setMessage(some);
            var result = this.lookupMovieSource(update, message);
            execute(message);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void findAMovie(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setReplyToMessageId(update.getMessage().getMessageId());
        this.findAMovie(message, update.getMessage().getText());
    }

    @SneakyThrows
    private void findAMovie(SendMessage message, String keyword) {
        var result = movieDBClient.searchAMovie(keyword);
        var totalpage = result.getTotalPages().intValue();

        int n = 0;
        while (n <= totalpage) {
            var string = messageTemplates.makePopularMovieHtml(result, n);
            message.setText(string);
            message.setParseMode("HTML");
            var buttons = new ArrayList<InlineKeyboardButton>();
            buttons.add(InlineKeyboardButton.builder().text("DOWNLOAD TORRENT 🦜🏴‍☠️").callbackData("YTS_LOOKUP").build());
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
            message.setReplyMarkup(inlineKeyboardMarkup);
            execute(message);
            n++;
        }
    }

    @SneakyThrows
    private void answerWhereToWatch(Update update) {
        SendMessage message = new SendMessage();
        if (update.getMessage().getReplyToMessage() == null) {
            message.setText("‼ /wheretowatch should be tagged to a movie reply message");
            message.setChatId(update.getMessage().getChatId());
            message.setReplyToMessageId(update.getMessage().getMessageId());
            message.setParseMode("HTML");
            message.disableWebPagePreview();
            execute(message);
        } else {
            var link = """
                    <a href= "https://www.themoviedb.org/movie/%s/watch">Where to Watch</a>
                    """.formatted(update.getMessage().getReplyToMessage().getEntities().get(2).getUrl().substring(33));
            message.setText(link);
            message.setChatId(update.getMessage().getChatId());
            message.setReplyToMessageId(update.getMessage().getMessageId());
            message.setParseMode("HTML");
            message.enableWebPagePreview();
            execute(message);
        }
    }

    @SneakyThrows
    private void answerCallBackQueryForNextPopular(CallbackQuery callbackQuery) {
        SendMessage message = new SendMessage();
        message.setChatId(callbackQuery.getMessage().getChatId().toString());
        this.popularAction(message, callbackQuery.getData().substring(13));
    }

    @SneakyThrows
    private void answerCallBackQuery(CallbackQuery callbackQuery) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText("Sending you the torrent file!");
        answerCallbackQuery.setShowAlert(Boolean.TRUE);

        execute(answerCallbackQuery);
        this.sendFile(callbackQuery.getData(), String.valueOf(callbackQuery.getMessage().getChatId()), callbackQuery.getMessage().getMessageId());
    }

    private void reply(Update update) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        if (!update.getMessage().isGroupMessage() && !update.getMessage().getFrom().getIsBot()) {
            replyToPrivateMessages(update, message);
        } else if (update.getMessage().isGroupMessage() && !update.getMessage().getFrom().getIsBot() && update.getMessage().getText().startsWith("@marcia_movie_bot") || update.getMessage().getText().startsWith("/start")) {
            replyToGroupMessages(update, message);
        } else if (update.getMessage().getReplyToMessage() != null && update.getMessage().getReplyToMessage().getFrom().getUserName().equals("marcia_movie_bot")) {
            replyToPrivateMessages(update, message);
        } else return;
        try {
            setReplyToAMessageId(message, update.getMessage().getMessageId());
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
        } else if (update.getMessage().getText().startsWith("/popular")) {
            this.popularAction(message, "1");
        } else {
            this.lookupMovieSource(update, message);
        }
    }

    private void replyToGroupMessages(Update update, SendMessage message) throws TelegramApiException {
        if (update.getMessage().getText().startsWith("/start")) {
            message.setParseMode("HTML");
            message.setText(Util.buildTelegramIntroMessage(update.getMessage().getFrom().getFirstName()));
        } else if (update.getMessage().getText().startsWith("/popular")) {
            popularAction(message, "1");
        } else {
            this.lookupMovieSource(update, message);
        }
    }

    private void popularAction(SendMessage message, String page) throws TelegramApiException {
        Integer pageInt = Integer.parseInt(page);
        pageInt++;
        int n = 0;
        while (n < 20) {
            var result = movieDBClient.getPopularMovies(page);
            var string = messageTemplates.makePopularMovieHtml(result, n);
            message.setText(string);
            message.setParseMode("HTML");
            if (n != 19) {
                execute(message);
            } else {
                var buttons = new ArrayList<InlineKeyboardButton>();
                buttons.add(InlineKeyboardButton.builder().text("NEXT").callbackData("NEXT_POPULAR_%d".formatted(pageInt)).build());
                InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
                message.setReplyMarkup(inlineKeyboardMarkup);
            }
            n++;
        }
    }

    private SendMessage lookupMovieSource(Update update, SendMessage message) {
        try {
            MovieInfo movieInfo;
            try {
                movieInfo = movieInfoCreatorService.buildMovieInfo(
                        ytsLookupService.buildARequestWithQuery(update.getMessage().getText()));
            } catch (IndexOutOfBoundsException e) {
                movieInfo = movieInfoCreatorService.buildMovieInfo(
                        ytsLookupService.buildARequestWithQuery(update.getMessage().getText().replaceAll("\\([^()]*\\)", "")));
            }
            if (update.getMessage().isGroupMessage() && update.getMessage().getText().startsWith("@marcia_movie_bot"))
                message.setText(messageTemplates.makeMovieFromYts(movieInfo));
            else
                message.setText(messageTemplates.makeMovieFromYts(movieInfo));

            var buttons = new ArrayList<InlineKeyboardButton>();

            movieInfo.getTorrentUrl().forEach((key, value) -> buttons.add(InlineKeyboardButton.builder().text(key).callbackData(value.substring(32)).build()));

            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
            message.setReplyMarkup(inlineKeyboardMarkup);
            message.setParseMode("HTML");
            message.enableWebPagePreview();
        } catch (Exception e) {
            log.error("error looking up movie", e);
            message.setText("I couldn't find what you are looking for \uD83D\uDC94");
            message.setParseMode("HTML");
            message.enableWebPagePreview();
        }
        return message;
    }

    private SendMessage setReplyToAMessageId(SendMessage outgoingMessage, Integer incomingId) {
        outgoingMessage.setReplyToMessageId(incomingId);
        return outgoingMessage;
    }

    @SneakyThrows
    private void sendFile(String hash, String chatId, int messageId) {
        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        InputFile file = new InputFile();
        file.setMedia(new URL("https://yts.mx/torrent/download/%s".formatted(hash)).openStream(), hash + ".torrent");
        document.setDocument(file);
        document.setReplyToMessageId(messageId);
        execute(document);
    }
}
