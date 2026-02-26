package com.reciclaje.web;

import com.reciclaje.ai.EnhancedAIResponder;
import com.reciclaje.ai.NeuralResponder;
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
    private final Chatbot neuralBot;
    private final EnhancedAIResponder aiBot;
    private final NeuralResponder neuralResponder;

    public ChatController() {
        this.ruleBot = new Chatbot(new RuleBasedResponder());
        
        // Inicializar bot neural
        this.neuralResponder = new NeuralResponder();
        this.neuralBot = new Chatbot(neuralResponder);
        
        this.aiBot = new EnhancedAIResponder();
        
        System.out.println("[ChatController] Bot neural inicializado - Aprendizaje: " + 
                          (neuralResponder.isNeuralEnabled() ? "ACTIVO" : "INACTIVO"));
        System.out.println("[ChatController] Conversaciones previas aprendidas: " + 
                          neuralResponder.getLearnedConversationCount());
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String mode = payload.getOrDefault("mode", "neural"); // "neural" (por defecto), "rules", "ai"

        String response;
        switch (mode.toLowerCase()) {
            case "ai":
                response = aiBot.respond(message);
                break;
            case "rules":
                response = ruleBot.reply(message);
                break;
            case "neural":
            default:
                // Use neural bot with learning capability
                response = neuralBot.reply(message);
                break;
        }

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("mode", mode);
        result.put("neural_enabled", String.valueOf(neuralResponder.isNeuralEnabled()));
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("neural_enabled", neuralResponder.isNeuralEnabled());
        stats.put("conversations_learned", neuralResponder.getLearnedConversationCount());
        stats.put("model_path", "data/neural-model/recycling-model.zip");
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping("/train")
    public ResponseEntity<Map<String, String>> trainModel() {
        try {
            neuralResponder.trainModel();
            neuralResponder.printStatistics();
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Modelo neural entrenado correctamente");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Error entrenando modelo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}