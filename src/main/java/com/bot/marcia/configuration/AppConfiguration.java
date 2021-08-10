package com.bot.marcia.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Thariq
 * @created 10-08-2021
 **/

@Data
@ConfigurationProperties(prefix = "application-configurations")
public class AppConfiguration {

    public String discordBotToken;

    public String telegramBotToken;

    public String ytsApiBaseUrl;
}
