package com.claude.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.claude.telegram.config.BotProperties;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(BotProperties.class)
public class ClaudeTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaudeTelegramBotApplication.class, args);
    }
}
