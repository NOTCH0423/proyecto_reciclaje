package com.reciclaje.core;

public class Chatbot {
    private Responder responder;

    public Chatbot(Responder responder) {
        this.responder = responder;
    }

    public String reply(String userMessage) {
        return responder.respond(userMessage);
    }

    public void setResponder(Responder responder) {
        this.responder = responder;
    }
}