package com.bot.marcia.telegram;

import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.dto.MoviedbPopular;
import com.bot.marcia.moviedb.*;
import com.bot.marcia.moviedb.dto.UpdateListRequestDTO;
import com.bot.marcia.moviedb.feign.MovieDbFeignClient;
import com.bot.marcia.moviedb.user.NoUserException;
import com.bot.marcia.moviedb.user.UserSession;
import com.bot.marcia.moviedb.user.UserSessionService;
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

import javax.swing.text.html.HTML;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static com.bot.marcia.telegram.CallBackCommand.*;

@RequiredArgsConstructor
@Slf4j
@Component
public class MarciaBot extends TelegramLongPollingBot {

    public final YtsLookupService ytsLookupService;

    public final MovieInfoCreatorService movieInfoCreatorService;

    private final MovieDBClient movieDBClient;

    private final MovieDbFeignClient movieDbFeignClient;

    @Value("${application-configurations.telegram-bot-token}")
    private String telegramBotToken;

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    private final MessageTemplates messageTemplates;

    private final TokenStorageRepository tokenStorageRepository;

    private final UserSessionService userSessionService;

    private final MovieComponent movieComponent;

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
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(YTS_LOOKUP.name())) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.answerCallForYtsLookup(update);
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(ADD_FAV.name())) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.updateList(message, true, true);
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Added to Favorites ✅");
            answerCallbackQuery.setShowAlert(Boolean.TRUE);
            execute(answerCallbackQuery);
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(REMOVE_FAV.name())) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.updateList(message, false, true);
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Removed from Favorites ❌");
            answerCallbackQuery.setShowAlert(Boolean.TRUE);
            execute(answerCallbackQuery);
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(REMOVE_WATCH.name())) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.updateList(message, false, false);
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Removed from watch list ❌");
            answerCallbackQuery.setShowAlert(Boolean.TRUE);
            execute(answerCallbackQuery);
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(ADD_WATCH.name())) {
            var message = (Message) update.getCallbackQuery().getMessage();
            update.setMessage(message);
            this.updateList(message, true, false);
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Added to watch list ✅");
            answerCallbackQuery.setShowAlert(Boolean.TRUE);
            execute(answerCallbackQuery);
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
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("NEXT_RECOMMEND_")) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            this.recommendMovies(message, update.getCallbackQuery().getData().replace("NEXT_RECOMMEND_", ""));
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("NEXT_FAV_LIST_")) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            this.getFavList(message, update.getCallbackQuery().getData().replace("NEXT_FAV_LIST_", ""));
        }
    }


    private void answerCallForPermission(Update update) throws TelegramApiException {
        try {
            this.saveSession(update.getCallbackQuery());
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Authorized with theMovieDB ✅");
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
        userSessionService.save(String.valueOf(callbackQuery.getMessage().getChatId())
                , account.getUsername(), sessionToken.session_id(), requestToken, accessToken.access_token(), account.getId().intValue(), String.valueOf(accessToken.account_id()));
    }

    private void executeCommands(String command, Update update) throws TelegramApiException {
        switch (command) {
            case "/start", "/help", "/hello": {
                SendMessage message = new SendMessage();
                message.setParseMode("HTML");
                message.setText(MessageTemplates.buildTelegramIntroMessage(update.getMessage().getChat().getFirstName()));
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
                var movie = movieDbFeignClient.getMovie(update.getMessage().getReplyToMessage().getEntities().get(1).getText());
                execute(this.lookupMovieSource(update, message, movie.getImdbId()));
                break;
            }
            case "/login": {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setReplyToMessageId(update.getMessage().getMessageId());
                var requestToken = movieDbFeignClient.createRequestToken();
                var text = """
                        1️⃣ <a href= "https://www.themoviedb.org/auth/access?request_token=%s">Log in to themoviedb.org</a>
                        2️⃣ Authorize bot
                        3️⃣ 👇🔻👇                     
                        """.formatted(requestToken.request_token());
                message.setParseMode("HTML");
                message.setText(text);
                var buttons = new ArrayList<InlineKeyboardButton>();
                TokenStorage tokenStorage = new TokenStorage();
                tokenStorage.setChatId(message.getChatId());
                tokenStorage.setRequestToken(requestToken.request_token());
                tokenStorageRepository.save(tokenStorage);
                buttons.add(InlineKeyboardButton.builder().text("Sign in with MovieDB ✅").callbackData("GENERATE_SESSION").build());
                InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttons).build();
                message.setReplyMarkup(inlineKeyboardMarkup);
                execute(message);
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
        var user = getUserSession(message);
        var watchList = movieDbFeignClient.getRecommendMovies(user.getAccountObjectId(), number);
        this.respondRecommendMessage(message, watchList, Integer.valueOf(number));
    }

    private UserSession getUserSession(SendMessage message) throws TelegramApiException {
        try {
            return userSessionService.getLoggedInUser(message.getChatId());
        } catch (NoUserException e) {
            message.setText(messageTemplates.makeNoUserMessage());
            message.setParseMode(ParseMode.HTML.name());
            super.execute(message);
        }
        throw new NoUserException();
    }

    private void updateList(Message message, boolean isAdd, boolean isFav) {
        var user = userSessionService.getLoggedInUser(String.valueOf(message.getChatId()));
        var listType = isFav ? "favorite" : "watchlist";
        var body = new UpdateListRequestDTO("movie", message.getEntities().get(1).getText(), !isFav, isAdd);
        movieDbFeignClient.addToList(listType, Integer.valueOf(user.getAccountId()), user.getSessionId(), body);
    }

    private void getFavList(SendMessage message, String pageNumber) throws TelegramApiException {
        var user = getUserSession(message);
        var watchList = movieDbFeignClient.getList("favorite", Integer.valueOf(user.getAccountId()), user.getSessionId(), pageNumber, "created_at.asc");
        this.respondFavListMessage(message, watchList, Integer.valueOf(pageNumber));
    }

    private void getWatchList(SendMessage message, String pageNumber) throws TelegramApiException {
        var user = getUserSession(message);
        var watchList = movieDBClient.getWatchList(Integer.valueOf(user.getAccountId()), user.getSessionId(), pageNumber);
        this.respondWatchListMessage(message, watchList, Integer.valueOf(pageNumber));
    }

    private void respondWatchListMessage(SendMessage message, MoviedbPopular watchList, Integer page) throws TelegramApiException {
        int n = watchList.getResults().size();
        var buttons = new ArrayList<InlineKeyboardButton>();
        if (n == 0) {
            message.setText("No Movies in watchlist!");
            message.setParseMode("HTML");
            execute(message);
        }
        buttons.add(InlineKeyboardButton.builder().text("Add Fav ♥").callbackData(ADD_FAV.name()).build());
        buttons.add(InlineKeyboardButton.builder().text("Remove ❌").callbackData(REMOVE_WATCH.name()).build());
        for (int i = 0; i < n; i++) {
            if (i == n - 1) {
                buttons.add(InlineKeyboardButton.builder().text("⏭").callbackData("NEXT_WATCH_LIST_%d".formatted(++page)).build());
            }
            message = movieComponent.getMovieMessage(message, watchList.getResults().get(i), i, buttons);
            execute(message);
        }
    }

    private void respondFavListMessage(SendMessage message, MoviedbPopular favList, Integer page) throws TelegramApiException {
        int n = favList.getResults().size();
        var buttons = new ArrayList<InlineKeyboardButton>();
        if (n == 0) {
            message.setText("No Favorites!");
            message.setParseMode("HTML");
            execute(message);
        }
        buttons.add(InlineKeyboardButton.builder().text("Later 📺").callbackData(ADD_WATCH.name()).build());
        buttons.add(InlineKeyboardButton.builder().text("Remove ❌").callbackData(REMOVE_FAV.name()).build());
        for (int i = 0; i < n; i++) {
            if (i == n - 1) {
                buttons.add(InlineKeyboardButton.builder().text("⏭").callbackData("NEXT_FAV_LIST_%d".formatted(++page)).build());
            }
            message = movieComponent.getMovieMessage(message, favList.getResults().get(i), i, buttons);
            execute(message);
        }
    }

    private void respondRecommendMessage(SendMessage message, MoviedbPopular recommendedList, Integer page) throws TelegramApiException {
        int n = recommendedList.getResults().size();
        var buttons = new ArrayList<InlineKeyboardButton>();
        if (n == 0) {
            message.setText("No Recommendations!");
            message.setParseMode("HTML");
            execute(message);
        }
        buttons.add(InlineKeyboardButton.builder().text("Add Fav ♥").callbackData(ADD_FAV.name()).build());
        buttons.add(InlineKeyboardButton.builder().text("Later 📺").callbackData(ADD_WATCH.name()).build());
        for (int i = 0; i < n; i++) {
            if (i == n - 1) {
                buttons.add(InlineKeyboardButton.builder().text("⏭").callbackData("NEXT_RECOMMEND_%d".formatted(++page)).build());
            }
            message = movieComponent.getMovieMessage(message, recommendedList.getResults().get(i), i, buttons);
            execute(message);
        }
    }

    private void popularAction(SendMessage message, int page) throws TelegramApiException {
        var popularMovies = movieDBClient.getPopularMovies(page);
        int n = popularMovies.getResults().size();
        var buttons = new ArrayList<InlineKeyboardButton>();
        buttons.add(InlineKeyboardButton.builder().text("Add Fav ♥").callbackData(ADD_FAV.name()).build());
        buttons.add(InlineKeyboardButton.builder().text("Later 📺").callbackData(ADD_WATCH.name()).build());
        for (int i = 0; i < n; i++) {
            if (i == n - 1) {
                buttons.add(InlineKeyboardButton.builder().text("⏭").callbackData("NEXT_POPULAR_%d".formatted(++page)).build());
            }
            message = movieComponent.getMovieMessage(message, popularMovies.getResults().get(i), i, buttons);
            execute(message);
        }
    }

    @SneakyThrows
    private void findAMovie(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setReplyToMessageId(update.getMessage().getMessageId());
        var searchedAMovie = movieDBClient.searchAMovie(update.getMessage().getText());
        int n = Math.min(searchedAMovie.getResults().size(), 6);
        var buttons = new ArrayList<InlineKeyboardButton>();
        buttons.add(InlineKeyboardButton.builder().text("Add Fav ♥").callbackData(ADD_FAV.name()).build());
        buttons.add(InlineKeyboardButton.builder().text("Later 📺").callbackData(ADD_WATCH.name()).build());
        for (int i = n - 1; i == 0; i--) {
            message = movieComponent.getMovieMessage(message, searchedAMovie.getResults().get(i), i, buttons);
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

    private void answerCallForYtsLookup(Update update) {
        try {
            SendMessage message = new SendMessage();
            var messageFromCallback = (Message) update.getCallbackQuery().getMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
            message.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
            message.setParseMode("HTML");
            message.enableWebPagePreview();
            var movie = movieDbFeignClient.getMovie(messageFromCallback.getEntities().get(1).getText());
            messageFromCallback.setText(messageFromCallback.getEntities().get(3).getText());
            update.setMessage(messageFromCallback);
            this.lookupMovieSource(update, message, movie.getImdbId());
            execute(message);
        } catch (Exception e) {
            log.error("", e);
        }
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
