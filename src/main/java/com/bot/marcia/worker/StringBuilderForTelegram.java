package com.bot.marcia.worker;


import com.bot.marcia.dto.MovieInfo;
import org.springframework.stereotype.Service;

@Service
public class StringBuilderForTelegram {

    public String buildMovieInfoTelegram(MovieInfo movieInfo){
        return String.format("<b>Source:</b>  %s\n \n" +
                "    " +
                "\n<b>Movie:</b> %s" +
                "\n<b>Description:</b> %s" +
                "\n\n<b>720p:</b> %s" +
                "\n\n<b>1080p:</b> %s" +
                "\n\n<b>4k:</b> %s \n",
                movieInfo.getYtsUrl(),
                movieInfo.getLongName(),
                movieInfo.getDesc(),
                movieInfo.getTorrentUrl().get("720p")==null?"<b> N/A </b>":movieInfo.getTorrentUrl().get("720p"),
                movieInfo.getTorrentUrl().get("1080p")==null?"<b> N/A </b>":movieInfo.getTorrentUrl().get("1080p"),
                movieInfo.getTorrentUrl().get("2160p")==null?"<b> N/A </b>":movieInfo.getTorrentUrl().get("2160p"));
    }

}
