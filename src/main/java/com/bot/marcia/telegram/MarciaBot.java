package com.bot.marcia.telegram;

import com.bot.marcia.common.Util;
import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.dto.MoviedbPopular;
import com.bot.marcia.moviedb.*;
import com.bot.marcia.moviedb.feign.MovieDbFeignClient;
import com.bot.marcia.yts.MovieInfoCreatorService;
import com.bot.marcia.yts.YtsLookupService;
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
import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
@Component
public class MarciaBot extends TelegramLongPollingBot {

    public final YtsLookupService ytsLookupService;

    public final MovieInfoCreatorService movieInfoCreatorService;

    private final MovieDBClient movieDBClient;

    private final UserSessionRepository userSessionRepository;

    private final MovieDbFeignClient movieDbFeignClient;

    @Value("${application-configurations.telegram-bot-token}")
    private String telegramBotToken;

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    private final MessageTemplates messageTemplates;

    private final TokenStorageRepository tokenStorageRepository;

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
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("GENERATE_SESSION")) {
            this.answerCallForPermission(update);
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("NEXT_WATCH_LIST_")) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            this.getWatchList(message, update.getCallbackQuery().getData().replace("NEXT_WATCH_LIST_", ""));
        }
    }

    private void answerCallForPermission(Update update) throws TelegramApiException {
        try {
            this.saveSession(update.getCallbackQuery());
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Authenticated with theMovieDB ✅");
            answerCallbackQuery.setShowAlert(Boolean.TRUE);
            execute(answerCallbackQuery);
        } catch (Exception e) {
            log.error("Failed to get a session", e);
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Failed to Authenticate with theMovieDB ❌");
            answerCallbackQuery.setShowAlert(Boolean.TRUE);
            execute(answerCallbackQuery);
        }
    }

    private void saveSession(CallbackQuery callbackQuery) {
        var requestToken = tokenStorageRepository.findById(String.valueOf(callbackQuery.getMessage().getChatId())).get().getRequestToken();
        var accessToken = movieDbFeignClient.createAccessToken("""
                {
                  "request_token": "%s"
                }
                """.formatted(requestToken));
        var sessionToken = movieDbFeignClient.createSession("""
                {
                  "access_token": "%s"
                }
                """.formatted(accessToken.access_token()));
        var account = movieDBClient.getAccountId(sessionToken.session_id());
        UserSession session = new UserSession();
        session.setChatId(Math.toIntExact(callbackQuery.getMessage().getChatId()));
        session.setUsername(account.getUsername());
        session.setSessionId(sessionToken.session_id());
        session.setRequestToken(requestToken);
        session.setAccessToken(accessToken.access_token());
        session.setAccountId(account.getId().intValue());
        session.setAccountObjectId(accessToken.account_id());
        userSessionRepository.save(session);
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
                update.getMessage().setText(update.getMessage().getReplyToMessage().getEntities().get(3).getText());
                var movie = movieDBClient.getMovie(update.getMessage().getReplyToMessage().getEntities().get(1).getText());
                execute(this.lookupMovieSource(update, message, movie.getImdbId()));
                break;
            }
            case "/login": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setReplyToMessageId(update.getMessage().getMessageId());
                var requestToken = movieDbFeignClient.createRequestToken();
                var text = """
                        Authorize me to themoviedb.org
                                                
                        - Click the link: <a href= "https://www.themoviedb.org/auth/access?request_token=%s">AUTHORIZE THE MOVIE DB</a>
                        - Authorize bot
                        - Come back and click Permission ✅
                        """.formatted(requestToken.request_token());
                message.setParseMode("HTML");
                message.setText(text);
                var buttons = new ArrayList<InlineKeyboardButton>();
                TokenStorage tokenStorage = new TokenStorage();
                tokenStorage.setChatId(message.getChatId());
                tokenStorage.setRequestToken(requestToken.request_token());
                tokenStorageRepository.save(tokenStorage);
                buttons.add(InlineKeyboardButton.builder().text("Permission ✅").callbackData("GENERATE_SESSION").build());
                InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
                message.setReplyMarkup(inlineKeyboardMarkup);
                execute(message);
                break;
            }
            case "/addtowatchlist": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.addToWatchList(update.getMessage(), true);
                break;
            }
            case "/watchlist": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.getWatchList(message, "1");
                break;
            }
            case "/fav": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.getFavList(message, "1");
                break;
            }
            case "/addtofav": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.addToFavList(update.getMessage(), true);
                break;
            }
            case "/removewatchlist": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.addToWatchList(update.getMessage(), false);
                break;
            }
            case "/removefav": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.addToFavList(update.getMessage(), false);
                break;
            }
            case "/recommend": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                this.recommendMovies(message, "1");
                break;
            }
            default:
                this.findAMovie(update);
                break;
        }
    }

    private void recommendMovies(SendMessage message, String number) throws TelegramApiException {
        var user = userSessionRepository.findById(Integer.valueOf(message.getChatId()));
        var watchList = movieDbFeignClient.getRecommendMovies(user.get().getAccountObjectId(), number);
        this.respondWatchListMessage(message, watchList, Integer.valueOf(number));
    }

    private void addToFavList(Message message, boolean add) {
        var user = userSessionRepository.findById(Math.toIntExact(message.getChatId()));
        movieDBClient.addToFavList(user.get().getAccountId(), user.get().getSessionId(), message.getReplyToMessage().getEntities().get(1).getText(), add);
    }

    private void getFavList(SendMessage message, String pageNumber) throws TelegramApiException {
        var user = userSessionRepository.findById(Integer.valueOf(message.getChatId()));
        var watchList = movieDBClient.getFavList(user.get().getAccountId(), user.get().getSessionId(), pageNumber);
        this.respondWatchListMessage(message, watchList, Integer.valueOf(pageNumber));
    }

    private void getWatchList(SendMessage message, String pageNumber) throws TelegramApiException {
        var user = userSessionRepository.findById(Integer.valueOf(message.getChatId()));
        var watchList = movieDBClient.getWatchList(user.get().getAccountId(), user.get().getSessionId(), pageNumber);
        this.respondWatchListMessage(message, watchList, Integer.valueOf(pageNumber));
    }

    private void respondWatchListMessage(SendMessage message, MoviedbPopular watchList, Integer page) throws TelegramApiException {
        int n = watchList.getResults().size();
        if (n == 0) {
            message.setText("No Movies in watchlist, Add some /addtowatchlist");
            message.setParseMode("HTML");
            execute(message);
        }
        for (int i = 0; i < n; i++) {
            var string = messageTemplates.makePopularMovieHtml(watchList.getResults().get(i), i);
            message.setText(string);
            message.setParseMode("HTML");
            var downloadButton = new ArrayList<InlineKeyboardButton>();
            downloadButton.add(InlineKeyboardButton.builder().text("DOWNLOAD TORRENT 🦜🏴‍☠️").callbackData("YTS_LOOKUP").build());
            InlineKeyboardMarkup downloadMarkupKeyboard = InlineKeyboardMarkup.builder().keyboardRow(downloadButton).build();
            message.setReplyMarkup(downloadMarkupKeyboard);
            if (i == n - 1) {
                var buttons = new ArrayList<InlineKeyboardButton>();
                buttons.add(downloadButton.get(0));
                buttons.add(InlineKeyboardButton.builder().text("NEXT").callbackData("NEXT_WATCH_LIST_%d".formatted(++page)).build());
                InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
                message.setReplyMarkup(inlineKeyboardMarkup);
            }
            execute(message);
        }
    }

    private void addToWatchList(Message message, boolean add) {
        var user = userSessionRepository.findById(Math.toIntExact(message.getChatId()));
        var response = movieDBClient.addToWatchList(user.get().getAccountId(), user.get().getSessionId(), message.getReplyToMessage().getEntities().get(1).getText(), add);
    }

    private void answerCallForYtsLookup(Update update) {
        try {
            SendMessage message = new SendMessage();
            var messageFromCallback = (Message) update.getCallbackQuery().getMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
            message.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
            message.setParseMode("HTML");
            message.enableWebPagePreview();
            var movie = movieDBClient.getMovie(messageFromCallback.getEntities().get(1).getText());
            messageFromCallback.setText(messageFromCallback.getEntities().get(3).getText());
            update.setMessage(messageFromCallback);
            this.lookupMovieSource(update, message, movie.getImdbId());
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
            if (i == n - 1) {
                var buttons = new ArrayList<InlineKeyboardButton>();
                buttons.add(InlineKeyboardButton.builder().text("NEXT").callbackData("NEXT_POPULAR_%d".formatted(++page)).build());
                InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
                message.setReplyMarkup(inlineKeyboardMarkup);
            }
            execute(message);
        }
    }

    @SneakyThrows
    private void findAMovie(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setReplyToMessageId(update.getMessage().getMessageId());
        var result = movieDBClient.searchAMovie(update.getMessage().getText());

        int n = Math.min(result.getResults().size(), 5);
        for (int i = n - 1; i >= 0; i--) {
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


    private SendMessage lookupMovieSource(Update update, SendMessage message, String imdbId) {
        try {
            MovieInfo movieInfo;
            try {
                movieInfo = movieInfoCreatorService.buildMovieInfo(ytsLookupService.buildARequestWithQuery(imdbId));
            } catch (IndexOutOfBoundsException e) {
                movieInfo = movieInfoCreatorService.buildMovieInfo(ytsLookupService.buildARequestWithQuery(imdbId));
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
