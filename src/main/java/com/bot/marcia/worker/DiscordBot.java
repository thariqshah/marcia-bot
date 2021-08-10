package com.bot.marcia.worker;

import com.bot.marcia.common.Resources;
import com.bot.marcia.service.impl.YtsLookupService;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiscordBot {

    @Autowired
    private YtsLookupService ytsLookupService;

    @Autowired
    private TorrentFileWorker torrentFileWorker;

    public void initDiscord() {

        DiscordApi api = new DiscordApiBuilder().
                setToken(Resources.discordBotToken)
                .login()
                .join();

        api.addMessageCreateListener(event -> {
            if (!event.getMessageAuthor().isBotUser()) {
                if (event.getMessage().getContent().startsWith("-find")) {
                    event.getChannel().sendMessage(
                            torrentFileWorker.returnTorrentUrlsFromYTS(ytsLookupService.buildARequestWithQuery(event.getMessage().getContent().substring(4))).toString());
                }
            }
        });
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());

    }

}
