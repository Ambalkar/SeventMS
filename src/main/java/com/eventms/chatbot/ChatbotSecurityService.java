package com.eventms.chatbot;

import com.eventms.auth.TokenStore;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
public class ChatbotSecurityService {

    private final HttpServletRequest request;

    public ChatbotSecurityService(HttpServletRequest request) {
        this.request = request;
    }

    public String getRole(HttpSession session) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            TokenStore.UserInfo user = TokenStore.getUser(token);
            if (user != null) {
                return user.getRole();
            }
        }
        if (session != null && session.getAttribute("authRole") != null) {
            return session.getAttribute("authRole").toString();
        }
        return "ANONYMOUS";
    }
}
