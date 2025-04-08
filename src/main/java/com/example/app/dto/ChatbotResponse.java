package com.example.app.dto;

public class ChatbotResponse {

    private String response;

    public ChatbotResponse() {
    }

    public ChatbotResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}