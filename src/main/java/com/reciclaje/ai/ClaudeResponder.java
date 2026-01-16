package com.reciclaje.ai;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.reciclaje.core.Responder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClaudeResponder implements Responder {
    private static final String DEFAULT_URL = "https://api.anthropic.com/v1/complete";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private final String apiKey;
    private final String apiUrl;
    private static final Logger LOGGER = Logger.getLogger(ClaudeResponder.class.getName());

    public ClaudeResponder(String apiKey) {
        this(apiKey, DEFAULT_URL);
    }

    public ClaudeResponder(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl == null || apiUrl.isBlank() ? DEFAULT_URL : apiUrl;
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String respond(String input) {
        if (apiKey == null || apiKey.isBlank()) {
            return "(IA no configurada: falta CLAUDE_API_KEY)";
        }

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("model", "claude-3.5");
            // simple prompt format compatible with several Anthropic endpoints
            String prompt = "Human: " + input + "\n\nAssistant:";
            payload.addProperty("prompt", prompt);
            payload.addProperty("max_tokens_to_sample", 512);

            RequestBody body = RequestBody.create(gson.toJson(payload), JSON);
            Request req = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .header("X-Api-Key", apiKey)
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                String rbody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    LOGGER.log(Level.WARNING, "Claude API returned HTTP {0}: {1}", new Object[]{resp.code(), rbody});
                    return "(Error al contactar la IA: HTTP " + resp.code() + ")";
                }
                JsonObject obj = gson.fromJson(rbody, JsonObject.class);
                // Try common fields used by different Anthropic responses
                if (obj == null) return rbody;
                if (obj.has("completion")) return trimCompletion(obj.get("completion"));
                if (obj.has("output")) return trimCompletion(obj.get("output"));
                if (obj.has("completion_text")) return trimCompletion(obj.get("completion_text"));
                if (obj.has("text")) return trimCompletion(obj.get("text"));
                // Fallback: search for first string value
                for (String k : obj.keySet()) {
                    JsonElement el = obj.get(k);
                    if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                        return el.getAsString();
                    }
                }
                return rbody;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error calling Claude API", ex);
            return "(Error al contactar la IA: " + ex.getMessage() + ")";
        }
    }

    private String trimCompletion(JsonElement el) {
        try {
            if (el.isJsonPrimitive()) return el.getAsString().trim();
            if (el.isJsonObject()) {
                JsonObject o = el.getAsJsonObject();
                if (o.has("text")) return o.get("text").getAsString().trim();
            }
        } catch (Exception ignored) {}
        return el.toString();
    }
}
