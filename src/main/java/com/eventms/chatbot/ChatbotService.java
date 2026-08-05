package com.eventms.chatbot;

import com.eventms.chatbot.model.ChatbotResponse;
import com.eventms.chatbot.model.ProjectKnowledge;
import com.eventms.chatbot.model.SourceReference;
import com.eventms.controller.ApiAuthController;
import com.eventms.controller.ApiController;
import com.eventms.controller.ApiAdminController;
import com.eventms.controller.AuthController;
import com.eventms.controller.AdminController;
import com.eventms.controller.UserController;
import com.eventms.service.FileNotificationService;
import com.eventms.auth.TokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;

@Service
public class ChatbotService {

    private final KnowledgeService knowledgeService;
    private final ChatbotSecurityService securityService;

    @Autowired
    public ChatbotService(KnowledgeService knowledgeService, ChatbotSecurityService securityService) {
        this.knowledgeService = knowledgeService;
        this.securityService = securityService;
    }

    public ChatbotResponse answer(String query, HttpSession session) {
        ProjectKnowledge knowledge = knowledgeService.loadKnowledge();
        String userRole = securityService.getRole(session);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);

        String responseText = knowledgeService.queryKnowledge(query, knowledge, isAdmin);
        List<SourceReference> references = knowledgeService.findReferences(query, knowledge);

        return ChatbotResponse.of(responseText, references, isAdmin);
    }
}
