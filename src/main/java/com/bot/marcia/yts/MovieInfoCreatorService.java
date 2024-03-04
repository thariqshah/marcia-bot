package com.bot.marcia.service;

import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.dto.Torrent;
import com.bot.marcia.dto.YtsMovie;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Service
public class MovieInfoCreatorService {

    public MovieInfo buildMovieInfo(YtsMovie ytsJsonSchema) {

        var movie = ytsJsonSchema.getData().getMovie();
        var movieBuilder = MovieInfo.builder();
        var qualityUrlMap = new HashMap<String, String>();
        var torrentObjects = ytsJsonSchema.getData().getMovie().getTorrents();

        for (Torrent torrents : torrentObjects
        ) {
            qualityUrlMap.put(torrents.getQuality(), torrents.getUrl());
        }

        return movieBuilder
                .torrentUrl(qualityUrlMap)
                .name(movie.getTitleEnglish())
                .longName(movie.getTitleLong())
                .ytsUrl(movie.getUrl())
                .desc(movie.getDescriptionIntro())
                .year(movie.getYear().intValue())
                .coverImageUrl(movie.getLargeCoverImage())
                .build();
    }
}
