package com.claude.telegram.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "claude-bot")
public class BotProperties {

    private String botToken;
    private Long chatId;
    private String workingDirectory = System.getProperty("user.home");
    private String claudePath = "claude";
    private int timeoutSeconds = 120;
    private String downloadPath = System.getProperty("user.home") + "/Downloads/telegram";
}
