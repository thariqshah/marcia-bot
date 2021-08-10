package com.bot.marcia.worker;

import com.bot.marcia.common.Resources;
import com.bot.marcia.service.impl.YtsLookupService;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DiscordBot {

    @Autowired
    private YtsLookupService ytsLookupService;

    @Autowired
    private MovieInfoCreator movieInfoCreator;

    @Autowired
    private MovieEmbedBuilder embedBuilder;

    public void initDiscord() {

        DiscordApi api = new DiscordApiBuilder().
                setToken(Resources.discordBotToken)
                .login()
                .join();

        api.addMessageCreateListener(event -> {
            if (!event.getMessageAuthor().isBotUser()) {
                if (event.getMessage().getContent().startsWith("-find")) {
                    event.getChannel().sendMessage(embedBuilder.embedBuilder(movieInfoCreator.buildMovieInfo(ytsLookupService.buildARequestWithQuery(event.getMessageContent().substring(4)))));
                }
            }
        });
        log.info("You can invite the bot by using the following url: {} ", api.createBotInvite());
    }

}
