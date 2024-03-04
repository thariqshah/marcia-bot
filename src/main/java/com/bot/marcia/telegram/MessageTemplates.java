package com.bot.marcia.telegram;


import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.dto.Result;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplates {

    public String makePopularMovieHtml(Result moviedbPopular, int index) {
        return """
                <b>#%d</b>\n
                <b>Title:</b> <a href="https://www.themoviedb.org/movie/%s">%s</a>\n
                <b>Description:</b> %s
                """.formatted(index + 1,
                moviedbPopular.getId(),
                makeTitle(moviedbPopular),
                moviedbPopular.getOverview());
    }

    private static String makeTitle(Result moviedbPopular) {
        return moviedbPopular.getOriginalTitle() + " (" + moviedbPopular.getReleaseDate().substring(0, 4) + ")";
    }

    public String makeMovieFromYts(MovieInfo movieInfo) {
        return """
                <b>Title:</b> <a href="%s">%s</a>\n
                <b>Description:</b> %s\n\n""".formatted(
                movieInfo.getYtsUrl(),
                movieInfo.getLongName(),
                movieInfo.getDesc());
    }
}
