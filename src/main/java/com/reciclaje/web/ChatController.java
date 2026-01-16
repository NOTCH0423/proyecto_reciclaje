package com.reciclaje.web;

import com.reciclaje.ai.EnhancedAIResponder;
import com.reciclaje.core.Chatbot;
import com.reciclaje.core.RuleBasedResponder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Allow frontend to call API
public class ChatController {

    private final Chatbot ruleBot;
    private final EnhancedAIResponder aiBot;

    public ChatController() {
        this.ruleBot = new Chatbot(new RuleBasedResponder());
        this.aiBot = new EnhancedAIResponder();
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String mode = payload.getOrDefault("mode", "auto"); // "auto" (rules), "ai" (openai)

        String response;
        if ("ai".equalsIgnoreCase(mode)) {
            response = aiBot.respond(message);
        } else {
            // Default to rule-based for speed and reliability unless explicit AI requested
            // Or could mix: try rules, if fallback, use AI.
            response = ruleBot.reply(message);
        }

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return ResponseEntity.ok(result);
    }
}