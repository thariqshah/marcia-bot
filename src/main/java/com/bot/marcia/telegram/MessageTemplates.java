package com.bot.marcia.telegram;


import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.dto.Result;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplates {

    public String makePopularMovieHtml(Result moviedbPopular, long index) {
        return """
                <b>#%d</b> TMDB ID - <b>%s</b>\n
                <b>Title:</b> <a href="https://www.themoviedb.org/movie/%s">%s</a>\n
                <b>Description:</b> %s
                """.formatted(index + 1,
                moviedbPopular.getId().intValue(),
                moviedbPopular.getId().intValue(),
                makeTitle(moviedbPopular),
                moviedbPopular.getOverview());
    }

    private static String makeTitle(Result moviedbPopular) {
        return moviedbPopular.getReleaseDate() != null && !moviedbPopular.getReleaseDate().isBlank() ?
                moviedbPopular.getOriginalTitle() + " (" + moviedbPopular.getReleaseDate().substring(0, 4) + ")" :
                moviedbPopular.getOriginalTitle();
    }

    public String makeMovieFromYts(MovieInfo movieInfo) {
        return """
                <b>Title:</b> <a href="%s">%s</a>\n
                <b>Description:</b> %s\n\n""".formatted(
                movieInfo.getYtsUrl(),
                movieInfo.getLongName(),
                movieInfo.getDesc());
    }
    public static String buildTelegramIntroMessage(String name) {
        return """
                Hello %s ,
                
                Feeds on themoviedb.org for movie information.
                
                👉 /login to Authorize with themoviedb.org
                👉 /popular to see trending movies
                👉 /recommend get recommendations
                👉 List /fav, /watchlist
                👉 Send a movie name to search for it.
                """.formatted(name);
    }

    public String makeNoUserMessage() {
        return """
                🔓 Not logged in with themoviedb.org
                Action requires moviedb account
                try again with /login
                """;
    }
}
