package com.bot.marcia;

import com.bot.marcia.service.impl.YtsLookupService;
import com.bot.marcia.worker.DiscordBot;
import com.bot.marcia.worker.MovieInfoCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MarciaApplication implements CommandLineRunner {

    @Autowired
    DiscordBot discordBot;

    public static void main(String[] args) {
        SpringApplication.run(MarciaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        discordBot.initDiscord();
    }
}
