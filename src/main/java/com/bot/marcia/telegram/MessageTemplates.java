package com.bot.marcia.telegram;


import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.dto.MoviedbPopular;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplates {

    public String makePopularMovieHtml(MoviedbPopular moviedbPopular,int index) {
        return """
                <b>#%d</b>\n
                <b>Title:</b> <a href="https://www.themoviedb.org/movie/%s">%s</a>\n
                <b>Description:</b> %s
                """.formatted(index+1,
                moviedbPopular.getResults().get(index).getId(),
                makeTitle(moviedbPopular, index),
                moviedbPopular.getResults().get(index).getOverview());
    }

    private static String makeTitle(MoviedbPopular moviedbPopular, int index) {
        return moviedbPopular.getResults().get(index).getOriginalTitle() + " (" + moviedbPopular.getResults().get(index).getReleaseDate().substring(0, 4) + ")";
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
