package com.claude.telegram.bot;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.claude.telegram.config.BotProperties;
import com.claude.telegram.service.ClaudeCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeCodeBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final int TELEGRAM_MAX_LENGTH = 4096;

    private final BotProperties properties;
    private final TelegramClient telegramClient;
    private final ClaudeCodeService claudeCodeService;

    @Override
    public String getBotToken() {
        return properties.getBotToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        long chatId = update.getMessage().getChatId();

        if (chatId != properties.getChatId().longValue()) {
            log.warn("Unauthorized access attempt from chatId: {}", chatId);
            return;
        }

        // Handle documents
        if (update.getMessage().hasDocument()) {
            handleDocument(chatId, update.getMessage().getDocument(), update.getMessage().getCaption());
            return;
        }

        // Handle photos
        if (update.getMessage().hasPhoto()) {
            List<PhotoSize> photos = update.getMessage().getPhoto();
            PhotoSize largest = photos.get(photos.size() - 1);
            handlePhoto(chatId, largest, update.getMessage().getCaption());
            return;
        }

        // Handle text
        if (!update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText().trim();
        log.info("Message from {}: {}", chatId, text);

        if (text.startsWith("/")) {
            handleCommand(chatId, text);
        } else {
            handleClaudeQuery(chatId, text);
        }
    }

    private void handleDocument(long chatId, Document document, String caption) {
        try {
            String fileName = document.getFileName();
            Path saved = downloadFile(document.getFileId(), fileName);
            log.info("Document saved: {}", saved);
            sendMessage(chatId, "Archivo guardado: `" + saved + "`");

            String prompt;
            if (caption != null && !caption.isBlank()) {
                prompt = caption + "\n\nThe file is located at: " + saved + "\nRead the file and use it to answer.";
            } else {
                prompt = "Read and analyze the file at: " + saved;
            }
            handleClaudeQuery(chatId, prompt);
        } catch (Exception e) {
            log.error("Failed to download document", e);
            sendMessage(chatId, "Error al descargar archivo: " + e.getMessage());
        }
    }

    private void handlePhoto(long chatId, PhotoSize photo, String caption) {
        try {
            String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
            Path saved = downloadFile(photo.getFileId(), fileName);
            log.info("Photo saved: {}", saved);
            sendMessage(chatId, "Foto guardada: `" + saved + "`");

            String prompt;
            if (caption != null && !caption.isBlank()) {
                prompt = caption + "\n\nThe image is located at: " + saved + "\nRead the image file and use it to answer.";
            } else {
                prompt = "Read and describe the image at: " + saved;
            }
            handleClaudeQuery(chatId, prompt);
        } catch (Exception e) {
            log.error("Failed to download photo", e);
            sendMessage(chatId, "Error al descargar foto: " + e.getMessage());
        }
    }

    private Path downloadFile(String fileId, String fileName) throws Exception {
        GetFile getFile = new GetFile(fileId);
        org.telegram.telegrambots.meta.api.objects.File telegramFile = telegramClient.execute(getFile);

        String fileUrl = "https://api.telegram.org/file/bot" + properties.getBotToken() + "/" + telegramFile.getFilePath();

        Path downloadDir = Path.of(properties.getDownloadPath());
        Files.createDirectories(downloadDir);
        Path target = downloadDir.resolve(fileName);

        try (InputStream in = new java.net.URI(fileUrl).toURL().openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target;
    }

    private void handleCommand(long chatId, String text) {
        String[] parts = text.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "/help" -> sendMessage(chatId, """
                    *Claude Code Bot* - Commands:

                    /help - Show this help
                    /new - Start a new conversation (clear context)
                    /session - Show current session ID
                    /pwd - Show current working directory
                    /cd <path> - Change working directory
                    /timeout <seconds> - Set command timeout
                    /downloads - Show downloads folder

                    Send any file or photo to save it.
                    Any other text is sent to Claude Code.""");

            case "/new" -> sendMessage(chatId, claudeCodeService.newSession());

            case "/session" -> {
                String sid = claudeCodeService.getSessionId();
                sendMessage(chatId, sid != null
                        ? "Session: `" + sid + "`"
                        : "No active session. Next message will start a new one.");
            }

            case "/pwd" -> sendMessage(chatId, "Working directory: `" + claudeCodeService.getWorkingDirectory() + "`");

            case "/downloads" -> sendMessage(chatId, "Downloads folder: `" + properties.getDownloadPath() + "`");

            case "/cd" -> {
                if (parts.length < 2) {
                    sendMessage(chatId, "Usage: /cd <path>");
                    return;
                }
                String result = claudeCodeService.changeDirectory(parts[1].trim());
                sendMessage(chatId, result);
            }

            case "/timeout" -> {
                if (parts.length < 2) {
                    sendMessage(chatId, "Current timeout: " + properties.getTimeoutSeconds() + "s\nUsage: /timeout <seconds>");
                    return;
                }
                try {
                    int seconds = Integer.parseInt(parts[1].trim());
                    properties.setTimeoutSeconds(seconds);
                    sendMessage(chatId, "Timeout set to " + seconds + "s");
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Invalid number: " + parts[1]);
                }
            }

            default -> sendMessage(chatId, "Unknown command. Use /help for available commands.");
        }
    }

    private void handleClaudeQuery(long chatId, String text) {
        sendMessage(chatId, "Processing...");

        claudeCodeService.executeAsync(text).thenAccept(response -> {
            sendLongMessage(chatId, response);
        }).exceptionally(ex -> {
            log.error("Error executing Claude command", ex);
            sendMessage(chatId, "Error: " + ex.getMessage());
            return null;
        });
    }

    private void sendLongMessage(long chatId, String text) {
        if (text.length() <= TELEGRAM_MAX_LENGTH) {
            sendMessage(chatId, text);
            return;
        }

        List<String> chunks = splitMessage(text);
        for (String chunk : chunks) {
            sendMessage(chatId, chunk);
        }
    }

    private List<String> splitMessage(String text) {
        List<String> chunks = new ArrayList<>();
        int maxLen = TELEGRAM_MAX_LENGTH;

        while (text.length() > maxLen) {
            int splitAt = text.lastIndexOf('\n', maxLen);
            if (splitAt <= 0) {
                splitAt = maxLen;
            }
            chunks.add(text.substring(0, splitAt));
            text = text.substring(splitAt).stripLeading();
        }
        if (!text.isEmpty()) {
            chunks.add(text);
        }
        return chunks;
    }

    private void sendMessage(long chatId, String text) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build();
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId {}: {}", chatId, e.getMessage());
        }
    }
}
