package com.bot.marcia.service;


import com.bot.marcia.dto.MovieInfo;
import org.springframework.stereotype.Service;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Service
public class StringBuilderForTelegram {

    public String buildMovieInfoTelegram(MovieInfo movieInfo) {
        return String.format("<b>Source:</b>  %s\n \n" +
                        "    " +
                        "\n<b>Movie:</b> %s" +
                        "\n<b>Description:</b> %s" +
                        "\n\n",
                movieInfo.getYtsUrl(),
                movieInfo.getLongName(),
                movieInfo.getDesc());
    }

}
