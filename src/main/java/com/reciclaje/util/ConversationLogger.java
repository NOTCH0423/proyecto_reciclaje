package com.reciclaje.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConversationLogger {
    private final Path logDir;
    private final Path sessionFile;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public ConversationLogger() {
        this.logDir = Path.of("logs");
        try {
            Files.createDirectories(logDir);
        } catch (IOException ignored) {}
        String name = "chat-" + LocalDateTime.now().format(fmt) + ".txt";
        this.sessionFile = logDir.resolve(name);
        try {
            Files.writeString(sessionFile, "# Chatbot Reciclaje\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException ignored) {}
    }

    public void logLine(String line) {
        try {
            Files.writeString(sessionFile, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    public Path saveSnapshot(String content) {
        Path snap = logDir.resolve("snapshot-" + LocalDateTime.now().format(fmt) + ".txt");
        try {
            Files.writeString(snap, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException ignored) {}
        return snap;
    }

    public Path getSessionFile() {
        return sessionFile;
    }
}