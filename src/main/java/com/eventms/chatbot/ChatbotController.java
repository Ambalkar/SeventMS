package com.eventms.chatbot;

import com.eventms.chatbot.model.ChatbotRequest;
import com.eventms.chatbot.model.ChatbotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/query")
    public ResponseEntity<ChatbotResponse> query(@RequestBody ChatbotRequest request, HttpSession session) {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ChatbotResponse.error("Ask a question so I can help you."));
        }
        request.setQuery(request.getQuery().trim());
        ChatbotResponse response = chatbotService.answer(request, session);
        return ResponseEntity.ok(response);
    }
}
