package com.bot.marcia.service;

import com.bot.marcia.dto.YtsJsonSchema;
import org.springframework.stereotype.Service;

@Service
public interface MovieLookupService {

    YtsJsonSchema buildARequestWithQuery(String query);
}
