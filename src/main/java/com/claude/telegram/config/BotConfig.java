package com.claude.telegram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BotConfig {

    private final BotProperties properties;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(properties.getBotToken());
    }
}
