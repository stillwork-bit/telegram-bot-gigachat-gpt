package org.bot.DTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private double temperature;
    @SerializedName("max_tokens")
    private int maxTokens;

    // Getters and Setters
    public ChatRequest(String model, List<ChatMessage> messages, double temperature, int maxTokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    // Fluent setters
    public ChatRequest addMessage(ChatMessage message) {
        this.messages.add(message);
        return this;
    }
}







