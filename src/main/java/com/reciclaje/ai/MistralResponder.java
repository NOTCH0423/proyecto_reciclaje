package com.reciclaje.ai;

import com.reciclaje.core.RecyclingKnowledgeBase;
import com.reciclaje.core.Responder;

/**
 * Deprecated placeholder for Mistral responder.
 * The UI no longer uses Mistral; this stub returns the fallback response.
 */
@Deprecated
public class MistralResponder implements Responder {
    private final RecyclingKnowledgeBase kb = new RecyclingKnowledgeBase();

    @Override
    public String respond(String input) {
        return kb.fallback() + "\n(IA Mistral deshabilitada: utiliza OpenAI o respuestas por reglas)";
    }
}