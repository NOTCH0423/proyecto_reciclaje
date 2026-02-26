package com.reciclaje.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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
 * Enhanced AI Responder with intelligent RAG (Retrieval Augmented Generation).
 * Features:
 * - Semantic search with scoring
 * - Conversation memory
 * - Intent analysis
 * - Context-aware responses
 * - Multi-model support (GPT-4 preferred)
 */
public class EnhancedAIResponder implements Responder {
    private static final String DEFAULT_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private final String apiKey;
    private final String apiUrl;
    private final List<KnowledgeChunk> knowledgeBase;
    private final List<ConversationMessage> conversationHistory;
    private static final int MAX_HISTORY = 10; // Keep last 10 messages for context

    public EnhancedAIResponder() {
        this(System.getenv("OPENAI_API_KEY"), DEFAULT_URL);
    }

    public EnhancedAIResponder(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl == null || apiUrl.isBlank() ? DEFAULT_URL : apiUrl;
        this.client = new OkHttpClient();
        this.knowledgeBase = loadKnowledgeBase();
        this.conversationHistory = new ArrayList<>();
    }

    private static class KnowledgeChunk {
        String topic;
        String content;
        Set<String> keywords;
        
        KnowledgeChunk(String topic, String content) {
            this.topic = topic;
            this.content = content;
            this.keywords = extractKeywords(content);
        }
        
        private Set<String> extractKeywords(String text) {
            Set<String> keywords = new HashSet<>();
            String lower = text.toLowerCase();
            // Extract important words (length > 3, not common stop words)
            String[] stopWords = {"los", "las", "que", "del", "para", "con", "por", "una", "son", "están"};
            for (String word : lower.split("\\s+")) {
                word = word.replaceAll("[^a-záéíóúñ]", "");
                if (word.length() > 3 && !Arrays.asList(stopWords).contains(word)) {
                    keywords.add(word);
                }
            }
            return keywords;
        }
        
        double calculateRelevance(String query) {
            String lowerQuery = query.toLowerCase();
            int matches = 0;
            int totalKeywords = keywords.size();
            
            // Exact keyword matches
            for (String keyword : keywords) {
                if (lowerQuery.contains(keyword)) {
                    matches++;
                }
            }
            
            // Topic relevance
            if (lowerQuery.contains(topic.toLowerCase().replace("[tema:", "").replace("]", "").trim())) {
                matches += 2;
            }
            
            // Content substring matches (for partial phrases)
            String[] queryWords = lowerQuery.split("\\s+");
            for (String word : queryWords) {
                if (word.length() > 3 && content.toLowerCase().contains(word)) {
                    matches++;
                }
            }
            
            return totalKeywords > 0 ? (double) matches / Math.max(totalKeywords, queryWords.length) : 0.0;
        }
    }

    private static class ConversationMessage {
        String role;
        String content;
        
        ConversationMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private List<KnowledgeChunk> loadKnowledgeBase() {
        List<KnowledgeChunk> chunks = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/training_data.txt")) {
            if (is == null) {
                System.err.println("Warning: training_data.txt not found in resources.");
                return chunks;
            }
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                StringBuilder currentChunk = new StringBuilder();
                String currentTopic = "";
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.trim().startsWith("[TEMA:")) {
                        if (currentChunk.length() > 0 && !currentTopic.isEmpty()) {
                            chunks.add(new KnowledgeChunk(currentTopic, currentChunk.toString()));
                            currentChunk.setLength(0);
                        }
                        currentTopic = line.trim();
                    } else if (!line.trim().startsWith("#") && !line.trim().isEmpty()) {
                        currentChunk.append(line).append("\n");
                    }
                }
                if (currentChunk.length() > 0 && !currentTopic.isEmpty()) {
                    chunks.add(new KnowledgeChunk(currentTopic, currentChunk.toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    private String retrieveContext(String query) {
        // Intelligent RAG with relevance scoring
        List<Map.Entry<KnowledgeChunk, Double>> scoredChunks = knowledgeBase.stream()
            .map(chunk -> new AbstractMap.SimpleEntry<>(chunk, chunk.calculateRelevance(query)))
            .filter(entry -> entry.getValue() > 0.1) // Minimum relevance threshold
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // Sort by relevance descending
            .limit(3) // Top 3 most relevant chunks
            .collect(Collectors.toList());

        if (scoredChunks.isEmpty()) return "";

        return scoredChunks.stream()
            .map(entry -> entry.getKey().content)
            .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String analyzeIntent(String query) {
        String lower = query.toLowerCase();
        
        // Intent classification
        if (lower.matches(".*(como|donde|que|cuando|por que|porque).*reciclar.*")) {
            return "HOW_TO_RECYCLE";
        }
        if (lower.matches(".*(que|que es|definicion|significa).*")) {
            return "DEFINITION";
        }
        if (lower.matches(".*(donde|punto|acopio|centro|lugar).*")) {
            return "LOCATION";
        }
        if (lower.matches(".*(calendario|dia|horario|cuando|recoleccion).*")) {
            return "SCHEDULE";
        }
        if (lower.matches(".*(consejo|tip|sugerencia|recomendacion|ayuda).*")) {
            return "ADVICE";
        }
        if (lower.matches(".*(puedo|se puede|es posible).*reciclar.*")) {
            return "FEASIBILITY";
        }
        
        return "GENERAL";
    }

    @Override
    public String respond(String input) {
        if (apiKey == null || apiKey.isBlank()) {
            return "⚠️ IA no configurada. Por favor, configura la variable de entorno OPENAI_API_KEY para usar el modelo inteligente.";
        }

        // Analyze intent and retrieve relevant context
        String intent = analyzeIntent(input);
        String context = retrieveContext(input);
        
        // Add user message to history
        conversationHistory.add(new ConversationMessage("user", input));
        if (conversationHistory.size() > MAX_HISTORY * 2) {
            // Keep only recent messages (remove oldest)
            conversationHistory.remove(0);
            conversationHistory.remove(0);
        }
        
        // Try GPT-4 first, fallback to GPT-3.5-turbo
        String model = "gpt-4";
        
        try {
            JsonObject root = new JsonObject();
            root.addProperty("model", model);
            
            JsonArray messages = new JsonArray();
            
            // Enhanced system prompt with context and intent awareness
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            
            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("Eres un experto en reciclaje, sostenibilidad y educación ambiental. ");
            systemPrompt.append("Tu personalidad es directa, educativa pero cercana, con un toque de humor inteligente. ");
            systemPrompt.append("Hablas como alguien de la Generación Z: claro, sin rodeos, pero con pasión por el planeta.\n\n");
            
            systemPrompt.append("INSTRUCCIONES IMPORTANTES:\n");
            systemPrompt.append("- Responde siempre en español\n");
            systemPrompt.append("- Sé específico y práctico en tus respuestas\n");
            systemPrompt.append("- Usa emojis moderadamente (♻️, 📦, 🌍, etc.)\n");
            systemPrompt.append("- Si no estás seguro, admítelo pero ofrece alternativas\n");
            systemPrompt.append("- Conecta las respuestas con el impacto ambiental real\n\n");
            
            if (!context.isEmpty()) {
                systemPrompt.append("CONTEXTO RELEVANTE DE LA BASE DE CONOCIMIENTOS:\n");
                systemPrompt.append(context);
                systemPrompt.append("\n\nUsa esta información como base, pero complementa con tu conocimiento general.\n");
            }
            
            systemPrompt.append("\nINTENCIÓN DETECTADA: ").append(intent);
            systemPrompt.append("\nAdapta tu respuesta según esta intención (cómo reciclar, definición, ubicación, etc.).\n");
            
            if (!conversationHistory.isEmpty() && conversationHistory.size() > 2) {
                systemPrompt.append("\nCONTEXTO DE CONVERSACIÓN: Esta es parte de una conversación continua. ");
                systemPrompt.append("Mantén coherencia con lo que se ha hablado anteriormente.\n");
            }
            
            sys.addProperty("content", systemPrompt.toString());
            messages.add(sys);
            
            // Add conversation history (last few exchanges)
            int historyStart = Math.max(0, conversationHistory.size() - 6);
            for (int i = historyStart; i < conversationHistory.size() - 1; i++) {
                ConversationMessage msg = conversationHistory.get(i);
                JsonObject histMsg = new JsonObject();
                histMsg.addProperty("role", msg.role);
                histMsg.addProperty("content", msg.content);
                messages.add(histMsg);
            }
            
            // Current user message
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", input);
            messages.add(user);
            
            root.add("messages", messages);
            root.addProperty("max_tokens", 1000);
            root.addProperty("temperature", 0.7); // Balance between creativity and accuracy
            
            RequestBody body = RequestBody.create(gson.toJson(root), JSON);
            Request req = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .build();

            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    String errorBody = resp.body() != null ? resp.body().string() : "";
                    // If GPT-4 fails, try GPT-3.5-turbo
                    if (resp.code() == 404 && model.equals("gpt-4")) {
                        return respondWithModel(input, "gpt-3.5-turbo", context);
                    }
                    return "Error de IA (" + resp.code() + "): " + (errorBody != null ? errorBody : "Error desconocido");
                }
                
                String rbody = resp.body().string();
                JsonObject o = gson.fromJson(rbody, JsonObject.class);
                if (o.has("choices") && o.getAsJsonArray("choices").size() > 0) {
                    String response = o.getAsJsonArray("choices").get(0).getAsJsonObject()
                        .get("message").getAsJsonObject()
                        .get("content").getAsString();
                    
                    // Add assistant response to history
                    conversationHistory.add(new ConversationMessage("assistant", response));
                    
                    return response;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to GPT-3.5-turbo if GPT-4 fails
            if (model.equals("gpt-4")) {
                try {
                    return respondWithModel(input, "gpt-3.5-turbo", context);
                } catch (Exception e2) {
                    return "Error interno al conectar con la IA: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido");
                }
            }
            return "Error interno al conectar con la IA: " + e.getMessage();
        }
        return "Lo siento, no pude procesar tu solicitud.";
    }
    
    private String respondWithModel(String input, String model, String context) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        JsonArray messages = new JsonArray();
        
        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        String systemPrompt = "Eres un experto en reciclaje y sostenibilidad. " +
            "Responde en español de forma clara, práctica y educativa. " +
            (context.isEmpty() ? "" : "Contexto: " + context);
        sys.addProperty("content", systemPrompt);
        messages.add(sys);
        
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", input);
        messages.add(user);
        
        root.add("messages", messages);
        root.addProperty("max_tokens", 1000);
        
        RequestBody body = RequestBody.create(gson.toJson(root), JSON);
        Request req = new Request.Builder()
            .url(apiUrl)
            .post(body)
            .header("Authorization", "Bearer " + apiKey)
            .build();
        
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                return "Error de IA (" + resp.code() + ")";
            }
            String rbody = resp.body().string();
            JsonObject o = gson.fromJson(rbody, JsonObject.class);
            if (o.has("choices") && o.getAsJsonArray("choices").size() > 0) {
                return o.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("content").getAsString();
            }
        }
        return "No se pudo obtener respuesta.";
    }
}