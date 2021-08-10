package com.bot.marcia.service.impl;


import com.bot.marcia.common.Resources;
import com.bot.marcia.dto.YtsJsonSchema;
import com.bot.marcia.service.MovieLookupService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class YtsLookupService implements MovieLookupService {

    @Override
    public YtsJsonSchema buildARequestWithQuery(String query) {
        WebClient.ResponseSpec client = WebClient
                .builder()
                .baseUrl(Resources.YTS_BASE_URL)
                .build().get().uri(uriBuilder -> uriBuilder.queryParam("query_term",query).build()).retrieve();
        return client.bodyToMono(YtsJsonSchema.class).block();
    }
}
