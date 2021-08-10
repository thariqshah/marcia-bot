package com.bot.marcia.service;

import org.springframework.stereotype.Service;

/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Service
public interface MovieLookupService {

    Object buildARequestWithQuery(String query);
}
