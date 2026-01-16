package com.reciclaje.api;

public class GetApiKeyExample {
    public static void main(String[] args) {
        String apiKeyId = null;
        if (args.length > 0) apiKeyId = args[0];
        if (apiKeyId == null || apiKeyId.isBlank()) {
            System.out.println("Usage: java com.reciclaje.api.GetApiKeyExample <api_key_id>");
            System.exit(1);
        }

        String adminKey = System.getenv("ANTHROPIC_ADMIN_API_KEY");
        ApiKeyClient client = new ApiKeyClient(adminKey);
        try {
            APIKey key = client.getApiKey(apiKeyId);
            System.out.println("Fetched API Key:");
            System.out.println(key);
        } catch (Exception e) {
            System.err.println("Error fetching API key: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
