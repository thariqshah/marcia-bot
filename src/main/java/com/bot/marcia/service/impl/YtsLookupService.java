package com.bot.marcia.service.impl;


import com.bot.marcia.dto.YtsJsonSchema;
import com.bot.marcia.service.MovieLookupService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Slf4j
@Service
public class YtsLookupService {
    //todo set timeout and retry, error specific exceptions

    public YtsJsonSchema buildARequestWithQuery(String query) {
        log.debug("Querying yts for movie info with : {} ", query);
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl("https://yts.mx/api/v2/list_movies.json")
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParam("query_term", query)
                        .queryParam("sort_by", "download_count")
                        .queryParam("order_by", "desc")
                        .build()).retrieve();
        return client.bodyToMono(YtsJsonSchema.class).block();
    }
}
