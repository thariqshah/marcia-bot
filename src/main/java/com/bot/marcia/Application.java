package com.bot.marcia;

import com.bot.marcia.telegram.MarciaBot;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
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
