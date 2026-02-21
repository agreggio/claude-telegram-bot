package com.claude.telegram.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.claude.telegram.config.BotProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClaudeCodeService {

    private final BotProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile File workingDirectory;
    private volatile String sessionId;

    public ClaudeCodeService(BotProperties properties) {
        this.properties = properties;
        this.workingDirectory = new File(properties.getWorkingDirectory());
    }

    public String getWorkingDirectory() {
        return workingDirectory.getAbsolutePath();
    }

    public String changeDirectory(String path) {
        File newDir;
        if (path.startsWith("/")) {
            newDir = new File(path);
        } else {
            newDir = new File(workingDirectory, path);
        }

        if (!newDir.exists()) {
            return "Directory not found: " + newDir.getAbsolutePath();
        }
        if (!newDir.isDirectory()) {
            return "Not a directory: " + newDir.getAbsolutePath();
        }

        workingDirectory = newDir;
        return "Changed to: `" + newDir.getAbsolutePath() + "`";
    }

    public String newSession() {
        sessionId = null;
        return "New session started. Next message will begin a fresh conversation.";
    }

    public String getSessionId() {
        return sessionId;
    }

    @Async
    public CompletableFuture<String> executeAsync(String prompt) {
        return CompletableFuture.completedFuture(execute(prompt));
    }

    private String execute(String prompt) {
        try {
            List<String> command = new ArrayList<>();
            command.add(properties.getClaudePath());
            command.add("-p");
            command.add(prompt);
            command.add("--output-format");
            command.add("json");
            command.add("--dangerously-skip-permissions");

            String currentSessionId = sessionId;
            if (currentSessionId != null) {
                command.add("--resume");
                command.add(currentSessionId);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workingDirectory);
            pb.redirectErrorStream(true);
            pb.redirectInput(ProcessBuilder.Redirect.from(new File("/dev/null")));

            // Remove all env vars that prevent claude from running inside another session
            Map<String, String> env = pb.environment();
            env.keySet().removeIf(key ->
                    key.startsWith("CLAUDE") || key.startsWith("ANTHROPIC"));

            log.info("Executing claude in {} (session={}): {}", workingDirectory, currentSessionId, prompt);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Command timed out after " + properties.getTimeoutSeconds() + "s";
            }

            int exitCode = process.exitValue();
            String rawOutput = output.toString().trim();

            if (exitCode != 0 && rawOutput.isEmpty()) {
                return "Process exited with code " + exitCode;
            }

            if (rawOutput.isEmpty()) {
                return "(empty response)";
            }

            return parseJsonResponse(rawOutput);

        } catch (Exception e) {
            log.error("Failed to execute claude command", e);
            return "Error executing command: " + e.getMessage();
        }
    }

    private String parseJsonResponse(String jsonOutput) {
        try {
            JsonNode root = objectMapper.readTree(jsonOutput);

            // Extract and store session_id for conversation continuity
            if (root.has("session_id") && !root.get("session_id").isNull()) {
                sessionId = root.get("session_id").asText();
                log.info("Session ID: {}", sessionId);
            }

            // Extract the text result
            if (root.has("result")) {
                String result = root.get("result").asText();
                return result.isEmpty() ? "(empty response)" : result;
            }

            // Fallback: return raw output if JSON doesn't have expected structure
            return jsonOutput;
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, returning raw output", e);
            return jsonOutput;
        }
    }
}
