package com.reciclaje.core;

import java.util.Locale;

public class RuleBasedResponder implements Responder {

    private final RecyclingKnowledgeBase kb = new RecyclingKnowledgeBase();
    private String userName = null;
    private RecyclingKnowledgeBase.QuizQuestion activeQuiz = null;

    @Override
    public String respond(String input) {
        if (input == null || input.trim().isEmpty()) {
            return kb.fallback();
        }
        String text = normalize(input);

        // Quiz Handling
        if (activeQuiz != null) {
            boolean correct = text.contains(activeQuiz.answerKeyword);
            String response = correct ? "✅ " + activeQuiz.explanation : "❌ Incorrecto. " + activeQuiz.explanation;
            activeQuiz = null;
            return response + "\n\n¿Quieres jugar otra vez? Escribe 'quiz'.";
        }

        if (containsAny(text, "quiz", "jugar", "trivia", "pregunta")) {
            activeQuiz = kb.getQuizQuestion();
            return "🧠 **PREGUNTA DE RECICLAJE** 🧠\n\n" + activeQuiz.question;
        }

        // Name capture
        if (text.startsWith("me llamo ") || text.startsWith("mi nombre es ")) {
            String name = extractName(input);
            if (name != null && !name.isEmpty()) {
                this.userName = name;
                return "¡Mucho gusto, " + name + "! 👋\nMe encanta conocer a gente comprometida con el planeta.\n¿En qué puedo ayudarte hoy?";
            }
        }

        // Greetings
        if (containsAny(text, "hola", "buenas", "buenos dias", "buenas noches", "hey")) {
             if (userName != null) {
                 return "¡Hola de nuevo, " + userName + "! ♻️\n¿Listo para seguir salvando el mundo?";
             }
            return kb.greeting();
        }
        // Goodbye
        if (containsAny(text, "adios", "hasta luego", "hasta pronto", "chao", "bye")) {
            return kb.goodbye();
        }
        // Time and date
        if (containsAny(text, "que hora es", "la hora", "horario")) {
            return kb.time();
        }
        if (containsAny(text, "que fecha", "hoy", "fecha")) {
            return kb.date();
        }
        // Weather
        if (containsAny(text, "clima", "tiempo", "lluvia", "sol", "nube")) {
            return kb.weather();
        }
        // Jokes
        if (containsAny(text, "chiste", "broma", "hace reir", "cuéntame un chiste")) {
            return kb.joke();
        }
        // Recycling specific
        if (containsAny(text, "dia", "dias", "recoleccion", "basura", "calendario", "horario")) {
            return kb.collectionDays();
        }
        if (containsAny(text, "reciclables", "reciclar", "residuos", "plastico", "papel", "carton", "vidrio", "metal")) {
            return kb.recyclables();
        }
        if (containsAny(text, "puntos", "acopio", "especial", "electronicos", "baterias", "aceite", "raee")) {
            return kb.specialPoints();
        }
        if (containsAny(text, "consejos", "tips", "aprovechar", "como reciclar", "buenas practicas")) {
            return kb.tips();
        }
        
        // Check for general questions first
        String generalResponse = kb.generalAnswer(input);
        if (generalResponse != null) {
            return generalResponse;
        }
        
        // Check for definition requests
        if (text.contains("definicion") || text.contains("que es")) {
            for (String word : new String[]{"sostenibilidad", "biodegradable", "contaminacion", "emisiones", "huella"}) {
                if (text.contains(word)) {
                    return kb.definition(word);
                }
            }
        }
        
        return kb.fallback();
    }

    private String extractName(String input) {
        String lower = input.toLowerCase();
        if (lower.startsWith("me llamo ")) {
            return input.substring(9).trim();
        }
        if (lower.startsWith("mi nombre es ")) {
            return input.substring(13).trim();
        }
        return null;
    }

    private String normalize(String s) {
        String t = s.toLowerCase(Locale.ROOT);
        t = t.replace("á", "a").replace("é", "e").replace("í", "i")
             .replace("ó", "o").replace("ú", "u").replace("ñ", "n");
        return t;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }
}