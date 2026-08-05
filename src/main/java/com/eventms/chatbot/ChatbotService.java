package com.eventms.chatbot;

import com.eventms.chatbot.model.ChatbotRequest;
import com.eventms.chatbot.model.ChatbotResponse;
import com.eventms.chatbot.model.SourceReference;
import com.example.eventmanagement.model.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ChatbotService {

    private final EventDao eventDao;
    private final ChatbotTemplateService templateService;
    private final String platformName;
    private final String supportEmail;
    private final String websiteUrl;

    public ChatbotService(
            EventDao eventDao,
            ChatbotTemplateService templateService,
            @Value("${platform.name:SeventMS}") String platformName,
            @Value("${support.email:devendraambalkar11@gmail.com}") String supportEmail,
            @Value("${platform.website:http://localhost:8080}") String websiteUrl) {
        this.eventDao = eventDao;
        this.templateService = templateService;
        this.platformName = platformName;
        this.supportEmail = supportEmail;
        this.websiteUrl = websiteUrl;
    }

    public ChatbotResponse answer(ChatbotRequest request, HttpSession session) {
        ChatIntent intent = resolveIntent(request);
        String query = request == null ? "" : safe(request.getQuery());
        String responseText = buildResponse(intent, request, session, query);
        return ChatbotResponse.of(responseText, List.<SourceReference>of(), isAdmin(session));
    }

    public ChatIntent resolveIntent(ChatbotRequest request) {
        if (request == null) {
            return ChatIntent.UNKNOWN;
        }

        String explicitIntent = safe(request.getIntent()).toUpperCase(Locale.ROOT);
        if (!explicitIntent.isEmpty()) {
            try {
                return ChatIntent.valueOf(explicitIntent);
            } catch (IllegalArgumentException ignored) {
            }
        }

        String buttonId = safe(request.getButtonId()).toLowerCase(Locale.ROOT);
        if (!buttonId.isEmpty()) {
            if (buttonId.contains("event")) return ChatIntent.VIEW_EVENTS;
            if (buttonId.contains("login") || buttonId.contains("register")) return ChatIntent.LOGIN_REGISTER;
            if (buttonId.contains("policy") || buttonId.contains("contract")) return ChatIntent.POLICY_DETAILS;
            if (buttonId.contains("support") || buttonId.contains("developer") || buttonId.contains("contact")) return ChatIntent.CONTACT_SUPPORT;
        }

        String query = safe(request.getQuery()).toLowerCase(Locale.ROOT);
        if (query.contains("event") || query.contains("seat") || query.contains("book")) {
            return ChatIntent.VIEW_EVENTS;
        }
        if (query.contains("login") || query.contains("register") || query.contains("sign up") || query.contains("signup")) {
            return ChatIntent.LOGIN_REGISTER;
        }
        if (query.contains("refund") || query.contains("policy") || query.contains("privacy") || query.contains("contract")) {
            return ChatIntent.POLICY_DETAILS;
        }
        if (query.contains("support") || query.contains("developer") || query.contains("contact")) {
            return ChatIntent.CONTACT_SUPPORT;
        }
        return ChatIntent.UNKNOWN;
    }

    private String buildResponse(ChatIntent intent, ChatbotRequest request, HttpSession session, String query) {
        switch (intent) {
            case VIEW_EVENTS:
                return buildUpcomingEventsResponse();
            case LOGIN_REGISTER:
                return buildLoginRegisterResponse(session);
            case POLICY_DETAILS:
                return buildPolicyDetailsResponse();
            case CONTACT_SUPPORT:
                return buildContactSupportResponse();
            case UNKNOWN:
            default:
                return buildUnknownResponse(query);
        }
    }

    private String buildUpcomingEventsResponse() {
        List<Event> events = eventDao.findUpcoming(5);
        if (events.isEmpty()) {
            String empty = templateService.get("chatbot.view_events.empty");
            return isBlank(empty) ? "I could not find any upcoming events right now. Please check the events page later." : empty;
        }

        StringBuilder response = new StringBuilder();
        String header = templateService.get("chatbot.view_events.header");
        if (!isBlank(header)) {
            response.append(header).append('\n');
        }
        for (Event event : events) {
            response.append(formatTemplate(templateService.get("chatbot.view_events.item"), event)).append('\n');
        }
        return response.toString().trim();
    }

    private String buildLoginRegisterResponse(HttpSession session) {
        String username = session != null && session.getAttribute("authName") != null
                ? session.getAttribute("authName").toString()
                : "";
        if (!isBlank(username)) {
            String loggedIn = templateService.get("chatbot.login.logged_in");
            return formatTemplate(loggedIn, username, null);
        }
        String loggedOut = templateService.get("chatbot.login.logged_out");
        return isBlank(loggedOut)
                ? "To book an event, please log in or create an account first. After that, return to the event page and continue booking."
                : loggedOut;
    }

    private String buildPolicyDetailsResponse() {
        String header = templateService.get("chatbot.policy.header");
        String body = templateService.get("chatbot.policy.body");
        if (isBlank(header) && isBlank(body)) {
            String fallback = templateService.get("chatbot.policy.fallback");
            return isBlank(fallback) ? "Please check the documentation page for policy details." : fallback;
        }
        StringBuilder response = new StringBuilder();
        if (!isBlank(header)) {
            response.append(header).append('\n');
        }
        if (!isBlank(body)) {
            response.append(body);
        }
        return response.toString().trim();
    }

    private String buildContactSupportResponse() {
        String template = templateService.get("chatbot.support");
        if (isBlank(template)) {
            String fallback = templateService.get("chatbot.support.fallback");
            return isBlank(fallback) ? "Please check the documentation page for support details." : fallback;
        }
        return template
                .replace("{support_email}", supportEmail)
                .replace("{website_url}", websiteUrl);
    }

    private String buildUnknownResponse(String query) {
        if (isBlank(query)) {
            return templateService.get("chatbot.greeting").replace("{platform_name}", platformName);
        }
        return "I am not sure about that yet. Please use the event, login, policy, or support buttons, or check the events page.";
    }

    private String formatTemplate(String template, Event event) {
        if (isBlank(template) || event == null) {
            return template;
        }
        return template
                .replace("{event_name}", safe(event.getName()))
                .replace("{event_date}", safe(event.getDate()))
                .replace("{event_location}", safe(event.getLocation()))
                .replace("{seats_left}", String.valueOf(event.getAvailableSpots()));
    }

    private String formatTemplate(String template, String username, Event event) {
        String resolved = isBlank(template)
                ? "You are logged in as {username}. You can browse events and continue booking from the event page."
                : template;
        return resolved.replace("{username}", safe(username));
    }

    private boolean isAdmin(HttpSession session) {
        return session != null && "ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("authRole")));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
