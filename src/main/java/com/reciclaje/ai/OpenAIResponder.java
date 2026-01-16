package com.reciclaje.ai;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * Simple OpenAI chat completions responder using OkHttp + Gson.
 * Reads API key from env `OPENAI_API_KEY`.
 */
public class OpenAIResponder implements Responder {
    private static final String DEFAULT_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private final String apiKey;
    private final String apiUrl;
    private static final Logger LOGGER = Logger.getLogger(OpenAIResponder.class.getName());

    public OpenAIResponder() {
        this(System.getenv("OPENAI_API_KEY"), DEFAULT_URL);
    }

    public OpenAIResponder(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl == null || apiUrl.isBlank() ? DEFAULT_URL : apiUrl;
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String respond(String input) {
        if (apiKey == null || apiKey.isBlank()) {
            return "(IA no configurada: falta OPENAI_API_KEY)";
        }

        try {
            JsonObject root = new JsonObject();
            root.addProperty("model", "gpt-3.5-turbo");
            JsonArray messages = new JsonArray();
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", "Eres un asistente educativo experto en reciclaje. Responde en español de forma clara y práctica.");
            messages.add(sys);
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", input);
            messages.add(user);
            root.add("messages", messages);
            root.addProperty("max_tokens", 512);
            root.addProperty("temperature", 0.7);

            RequestBody body = RequestBody.create(gson.toJson(root), JSON);
            Request req = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                String rbody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    LOGGER.log(Level.WARNING, "OpenAI API returned HTTP {0}: {1}", new Object[]{resp.code(), rbody});
                    return "(Error al contactar la IA: HTTP " + resp.code() + ")";
                }
                JsonObject o = gson.fromJson(rbody, JsonObject.class);
                if (o == null) return rbody;
                // navigate to choices[0].message.content
                if (o.has("choices")) {
                    try {
                        JsonArray choices = o.getAsJsonArray("choices");
                        if (choices.size() > 0) {
                            JsonObject first = choices.get(0).getAsJsonObject();
                            if (first.has("message")) {
                                JsonObject msg = first.getAsJsonObject("message");
                                if (msg.has("content")) return msg.get("content").getAsString().trim();
                                if (msg.has("role") && msg.has("content")) return msg.get("content").getAsString().trim();
                            }
                        }
                    } catch (Exception ex) {
                        // fallthrough to try other fields
                    }
                }
                if (o.has("error")) return o.get("error").toString();
                return rbody;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error calling OpenAI API", ex);
            return "(Error al contactar la IA: " + ex.getMessage() + ")";
        }
    }
}
