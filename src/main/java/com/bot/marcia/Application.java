package com.bot.marcia;

import com.bot.marcia.moviedb.feign.MovieDbFeignClient;
import com.bot.marcia.telegram.MarciaBot;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Slf4j
@EnableFeignClients
@SpringBootApplication
public class Application {

    private final MarciaBot marciaBot;

    public Application(MarciaBot marciaBot) {
        this.marciaBot = marciaBot;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void initBot() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(marciaBot);
    }


}
