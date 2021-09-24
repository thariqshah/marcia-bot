package com.bot.marcia;

import com.bot.marcia.configuration.AppConfiguration;
import com.bot.marcia.service.DiscordBotService;
import com.bot.marcia.service.TelegramBotInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Slf4j
@EnableConfigurationProperties(AppConfiguration.class)
@SpringBootApplication
@PropertySource("application.yml")
public class MarciaApplication implements CommandLineRunner {

    @Autowired
    private DiscordBotService discordBotService;

    @Autowired
    private TelegramBotInit telegramBotInit;

    public static void main(String[] args) {
        SpringApplication.run(MarciaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        discordBotService.initDiscord();
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBotInit);
    }

}
