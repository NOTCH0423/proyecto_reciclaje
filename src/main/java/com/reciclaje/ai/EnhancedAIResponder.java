package com.reciclaje.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.reciclaje.core.Responder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Enhanced AI Responder with RAG (Retrieval Augmented Generation).
 * Loads a text file (training_data.txt) and injects relevant sections into the prompt.
 */
public class EnhancedAIResponder implements Responder {
    private static final String DEFAULT_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private final String apiKey;
    private final String apiUrl;
    private final List<String> knowledgeBase;

    public EnhancedAIResponder() {
        this(System.getenv("OPENAI_API_KEY"), DEFAULT_URL);
    }

    public EnhancedAIResponder(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl == null || apiUrl.isBlank() ? DEFAULT_URL : apiUrl;
        this.client = new OkHttpClient();
        this.knowledgeBase = loadKnowledgeBase();
    }

    private List<String> loadKnowledgeBase() {
        List<String> chunks = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/training_data.txt")) {
            if (is == null) {
                System.err.println("Warning: training_data.txt not found in resources.");
                return chunks;
            }
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                StringBuilder currentChunk = new StringBuilder();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.trim().startsWith("[TEMA:")) {
                        if (currentChunk.length() > 0) {
                            chunks.add(currentChunk.toString());
                            currentChunk.setLength(0);
                        }
                    }
                    currentChunk.append(line).append("\n");
                }
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    private String retrieveContext(String query) {
        // Simple keyword matching RAG
        // In a real system, this would use embeddings and vector search
        String lowerQuery = query.toLowerCase();
        List<String> matches = knowledgeBase.stream()
            .filter(chunk -> {
                // Check if any significant word in the chunk matches the query
                // This is a naive heuristic
                String lowerChunk = chunk.toLowerCase();
                String[] queryWords = lowerQuery.split("\\s+");
                int matchCount = 0;
                for (String word : queryWords) {
                    if (word.length() > 3 && lowerChunk.contains(word)) {
                        matchCount++;
                    }
                }
                return matchCount > 0;
            })
            .collect(Collectors.toList());

        if (matches.isEmpty()) return "";

        return String.join("\n---\n", matches);
    }

    @Override
    public String respond(String input) {
        if (apiKey == null || apiKey.isBlank()) {
            return "⚠️ IA no configurada. Por favor, configura la variable de entorno OPENAI_API_KEY para usar el modelo inteligente.";
        }

        String context = retrieveContext(input);
        
        try {
            JsonObject root = new JsonObject();
            root.addProperty("model", "gpt-3.5-turbo");
            JsonArray messages = new JsonArray();
            
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            
            String systemPrompt = "Eres un experto en reciclaje y sostenibilidad. ";
            if (!context.isEmpty()) {
                systemPrompt += "Usa la siguiente información de contexto para responder si es relevante:\n" + context;
            } else {
                systemPrompt += "Responde basándote en tu conocimiento general sobre reciclaje.";
            }
            
            sys.addProperty("content", systemPrompt);
            messages.add(sys);
            
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", input);
            messages.add(user);
            
            root.add("messages", messages);
            root.addProperty("max_tokens", 800);
            
            RequestBody body = RequestBody.create(gson.toJson(root), JSON);
            Request req = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .header("Authorization", "Bearer " + apiKey)
                .build();

            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    return "Error de IA (" + resp.code() + "): No se pudo procesar la solicitud.";
                }
                
                String rbody = resp.body().string();
                JsonObject o = gson.fromJson(rbody, JsonObject.class);
                if (o.has("choices") && o.getAsJsonArray("choices").size() > 0) {
                    return o.getAsJsonArray("choices").get(0).getAsJsonObject()
                        .get("message").getAsJsonObject()
                        .get("content").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error interno al conectar con la IA.";
        }
        return "Lo siento, no pude procesar tu solicitud.";
    }
}