package com.bot.marcia.service;

import com.bot.marcia.dto.MovieInfo;
import com.bot.marcia.dto.YtsJsonSchema;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Service
public class MovieInfoCreatorService {

    public MovieInfo buildMovieInfo(YtsJsonSchema ytsJsonSchema) {

        var movie = (Map<String, Object>) ytsJsonSchema.getData().getMovies().get(0);
        var movieBuilder = MovieInfo.builder();
        var qualityUrlMap = new HashMap<String, String>();
        var torrentObjects = (List<Map<String, String>>) movie.get("torrents");

        for (Map<String, String> torrents : torrentObjects
        ) {
            qualityUrlMap.put(torrents.get("quality"), torrents.get("url"));
        }

        return movieBuilder
                .torrentUrl(qualityUrlMap)
                .name((String) movie.get("title_english"))
                .longName((String) movie.get("title_long"))
                .ytsUrl((String) movie.get("url"))
                .desc((String) movie.get("summary"))
                .year((Integer) movie.get("year"))
                .coverImageUrl((String) movie.get("large_cover_image"))
                .build();
    }
}
