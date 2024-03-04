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
        if (update.getMessage() != null && update.getMessage().isUserMessage())
            this.executeCommands(update.getMessage().getText(), update);
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("NEXT_POPULAR_")) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.answerCallBackQueryForNextPopular(update.getCallbackQuery());
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("YTS_LOOKUP")) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.answerCallForYtsLookup(update);
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().matches("([A-F\\d]{40})")) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.answerCallBackForTorrentHash(update.getCallbackQuery());
        }
    }

    private void executeCommands(String command, Update update) throws TelegramApiException {
        switch (command) {
            case "/start", "/help", "/hello": {
                SendMessage message = new SendMessage();
                message.setParseMode("HTML");
                message.setText(Util.buildTelegramIntroMessage(update.getMessage().getChat().getFirstName()));
                message.setChatId(update.getMessage().getChatId());
                execute(message);
                break;
            }
            case "/popular": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.popularAction(message, 1);
                break;
            }
            case "/wheretowatch": {
                this.answerWhereToWatch(update);
                break;
            }
            case "/download": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setReplyToMessageId(update.getMessage().getMessageId());
                update.getMessage().setText(update.getMessage().getReplyToMessage().getEntities().get(2).getText());
                execute(this.lookupMovieSource(update, message));
                break;
            }
            default:
                this.findAMovie(update);
                break;
                /*
                SendMessage message = new SendMessage();
                log.error("error looking up movie", e);
                message.setReplyToMessageId(update.getMessage().getMessageId());
                message.setChatId(update.getMessage().getChatId());
                message.setText("I couldn't find what you are looking for \uD83D\uDC94");
                message.setParseMode("HTML");
                message.enableWebPagePreview();
                execute(message);
                */
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
            this.lookupMovieSource(update, message);
            execute(message);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void popularAction(SendMessage message, int page) throws TelegramApiException {
        var results = movieDBClient.getPopularMovies(page);

        int n = results.getResults().size();
        for (int i = 0; i < n; i++) {
            var string = messageTemplates.makePopularMovieHtml(results.getResults().get(i), i);
            message.setText(string);
            message.setParseMode("HTML");
            execute(message);
            if (i == n - 1) {
                var buttons = new ArrayList<InlineKeyboardButton>();
                buttons.add(InlineKeyboardButton.builder().text("NEXT").callbackData("NEXT_POPULAR_%d".formatted(++page)).build());
                InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
                message.setReplyMarkup(inlineKeyboardMarkup);
            }
        }
    }

    @SneakyThrows
    private void findAMovie(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setReplyToMessageId(update.getMessage().getMessageId());
        var result = movieDBClient.searchAMovie(update.getMessage().getText());

        int n = result.getResults().size();
        for (int i = 0; i < n; i++) {
            message.setText(messageTemplates.makePopularMovieHtml(result.getResults().get(i), i));
            message.setParseMode("HTML");
            var buttons = new ArrayList<InlineKeyboardButton>();
            buttons.add(InlineKeyboardButton.builder().text("DOWNLOAD TORRENT 🦜🏴‍☠️").callbackData("YTS_LOOKUP").build());
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
            message.setReplyMarkup(inlineKeyboardMarkup);
            execute(message);
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
        this.popularAction(message, Integer.parseInt(callbackQuery.getData().substring(13)));
    }

    @SneakyThrows
    private void answerCallBackForTorrentHash(CallbackQuery callbackQuery) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText("Sending you the torrent file!");
        answerCallbackQuery.setShowAlert(Boolean.TRUE);
        execute(answerCallbackQuery);
        this.sendFile(callbackQuery.getData(), String.valueOf(callbackQuery.getMessage().getChatId()), callbackQuery.getMessage().getMessageId());
    }


    private SendMessage lookupMovieSource(Update update, SendMessage message) {
        try {
            MovieInfo movieInfo;
            try {
                movieInfo = movieInfoCreatorService.buildMovieInfo(ytsLookupService.buildARequestWithQuery(update.getMessage().getText()));
            } catch (IndexOutOfBoundsException e) {
                movieInfo = movieInfoCreatorService.buildMovieInfo(ytsLookupService.buildARequestWithQuery(update.getMessage().getText().replaceAll("\\([^()]*\\)", "")));
            }
            if (update.getMessage().isGroupMessage() && update.getMessage().getText().startsWith("@marcia_movie_bot"))
                message.setText(messageTemplates.makeMovieFromYts(movieInfo));
            else message.setText(messageTemplates.makeMovieFromYts(movieInfo));

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
