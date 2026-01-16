package com.reciclaje.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyClient {
    private static final String BASE_URL = "https://api.anthropic.com/v1/organizations/api_keys/";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String adminKey;
    private static final Logger LOGGER = Logger.getLogger(ApiKeyClient.class.getName());

    public ApiKeyClient(String adminKey) {
        this.adminKey = adminKey;
    }

    public APIKey getApiKey(String apiKeyId) throws IOException {
        if (adminKey == null || adminKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_ADMIN_API_KEY is not set");
        }

        String url = BASE_URL + apiKeyId;
        Request req = new Request.Builder()
                .url(url)
                .get()
                .header("X-Api-Key", adminKey)
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String body = resp.body() != null ? resp.body().string() : "";
                LOGGER.log(Level.WARNING, "Unexpected HTTP code: {0} - {1} - {2}", new Object[]{resp.code(), resp.message(), body});
                throw new IOException("Unexpected HTTP code: " + resp.code() + " - " + resp.message());
            }
            String body = resp.body() != null ? resp.body().string() : "";
            return gson.fromJson(body, APIKey.class);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to fetch API key: " + apiKeyId, ex);
            throw ex;
        }
    }
}
