package com.bot.marcia.yts;


import com.bot.marcia.dto.YtsMovie;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Slf4j
@Service
public class YtsLookupService {

    public YtsMovie buildARequestWithQuery(String imdbId) {
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl("https://yts.mx/api/v2/movie_details.json")
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParam("imdb_id", imdbId)
                        .build()).retrieve();
         return client.bodyToMono(YtsMovie.class).block();
    }
}
