package com.bot.marcia.service;

import com.bot.marcia.service.impl.YtsLookupService;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Thariq
 * @created 10-08-2021
 **/
@Slf4j
@Service
public class DiscordBotService {

    @Autowired
    private YtsLookupService ytsLookupService;

    @Autowired
    private MovieInfoCreatorService movieInfoCreatorService;

    @Autowired
    private MovieEmbedBuilderService embedBuilder;

    @Value("${application-configurations.discord-bot-token}")
    private String discordToken;

    public void initDiscord() {
        log.debug("Initializing discord bot");
        DiscordApi api = new DiscordApiBuilder().
                setToken(discordToken)
                .login()
                .join();
        log.debug("Initialized discord bot");
        api.addMessageCreateListener(event -> {
            if (!event.getMessageAuthor().isBotUser()) {
                if (!event.getMessage().getMentionedUsers().isEmpty() && event.getMessage().getMentionedUsers().get(0).getId() == 874578310955421716l) {
                    log.debug("message received via discord from user the {}", event.getMessageAuthor().getDisplayName());
                    event.getChannel().sendMessage(embedBuilder.embedBuilder(movieInfoCreatorService.buildMovieInfo(ytsLookupService.buildARequestWithQuery(event.getMessageContent().substring(22)))));
                }
            }
        });
        log.info("You can invite the bot by using the following url: {} ", api.createBotInvite());
    }

}
