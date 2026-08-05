package com.eventms.chatbot;

import com.eventms.chatbot.model.ChatbotResponse;
import com.eventms.chatbot.model.ProjectKnowledge;
import com.eventms.chatbot.model.SourceReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import javax.servlet.http.HttpSession;
import java.util.List;

@Service
public class ChatbotService {

    private final KnowledgeService knowledgeService;
    private final ChatbotSecurityService securityService;
    private final GeminiService geminiService;

    @Autowired
    public ChatbotService(KnowledgeService knowledgeService, ChatbotSecurityService securityService, GeminiService geminiService) {
        this.knowledgeService = knowledgeService;
        this.securityService = securityService;
        this.geminiService = geminiService;
    }

    public ChatbotResponse answer(String query, HttpSession session) {
        ProjectKnowledge knowledge = knowledgeService.loadKnowledge();
        String userRole = securityService.getRole(session);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);

        String responseText = knowledgeService.queryKnowledge(query, knowledge, isAdmin);
        List<SourceReference> references = knowledgeService.findReferences(query, knowledge);

        if (geminiService.isEnabled()) {
            Optional<String> geminiAnswer = geminiService.generate(query);
            if (geminiAnswer.isPresent()) {
                responseText = geminiAnswer.get();
            }
        }

        return ChatbotResponse.of(responseText, references, isAdmin);
    }
}
