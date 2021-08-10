package com.bot.marcia.worker;

import com.bot.marcia.dto.MovieInfo;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MovieEmbedBuilder {

    public EmbedBuilder embedBuilder(MovieInfo movieInfo) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(movieInfo.getName())
                .setDescription(movieInfo.getDesc())
                .setAuthor(movieInfo.getLongName(), movieInfo.getYtsUrl(), "https://yts.movie/logo.png")
                .setThumbnail(movieInfo.getCoverImageUrl());
        if (movieInfo.getTorrentUrl() != null) {
            for (Map.Entry<String, String> entry : movieInfo.getTorrentUrl().entrySet()) {
                embed.addField(entry.getKey(), entry.getValue(), true);
            }
        }
        return embed;
    }
}
