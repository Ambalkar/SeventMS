package com.eventms.chatbot.model;

import java.util.List;

public class ChatbotResponse {
    private String answer;
    private boolean success;
    private List<SourceReference> references;
    private boolean adminOnly;

    public static ChatbotResponse of(String answer, List<SourceReference> references, boolean adminOnly) {
        ChatbotResponse response = new ChatbotResponse();
        response.answer = answer;
        response.success = true;
        response.references = references;
        response.adminOnly = adminOnly;
        return response;
    }

    public static ChatbotResponse error(String message) {
        ChatbotResponse response = new ChatbotResponse();
        response.answer = message;
        response.success = false;
        return response;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<SourceReference> getReferences() {
        return references;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }
}
