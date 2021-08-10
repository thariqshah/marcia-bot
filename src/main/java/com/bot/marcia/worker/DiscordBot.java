package com.bot.marcia.worker;

import com.bot.marcia.common.Resources;
import com.bot.marcia.service.impl.YtsLookupService;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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
                if (!event.getMessage().getMentionedUsers().isEmpty()&&event.getMessage().getMentionedUsers().get(0).getId()==874578310955421716l) {
                    event.getChannel().sendMessage(embedBuilder.embedBuilder(movieInfoCreator.buildMovieInfo(ytsLookupService.buildARequestWithQuery(event.getMessageContent().substring(22)))));
                }
            }
        });
        log.info("You can invite the bot by using the following url: {} ", api.createBotInvite());
    }

}
