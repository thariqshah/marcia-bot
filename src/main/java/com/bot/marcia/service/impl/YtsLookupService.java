package com.bot.marcia.service.impl;


import com.bot.marcia.configuration.AppConfiguration;
import com.bot.marcia.dto.YtsJsonSchema;
import com.bot.marcia.service.MovieLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Slf4j
@RequiredArgsConstructor
@Service
public class YtsLookupService implements MovieLookupService {

    private final AppConfiguration appConfiguration;
    //todo set timeout and retry, error specific exceptions

    @Override
    public YtsJsonSchema buildARequestWithQuery(String query) {
        log.debug("Querying yts for movie info with : {} ", query);
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl(appConfiguration.getYtsApiBaseUrl())
                .build().get().uri(uriBuilder -> uriBuilder
                        .queryParam("query_term", query)
                        .queryParam("sort_by", "download_count")
                        .queryParam("order_by", "desc")
                        .build()).retrieve();
        return client.bodyToMono(YtsJsonSchema.class).block();
    }
}
