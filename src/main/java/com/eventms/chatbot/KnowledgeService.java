package com.eventms.chatbot;

import com.eventms.chatbot.model.ProjectKnowledge;
import com.eventms.chatbot.model.SourceReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {

    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));
    private final JdbcTemplate jdbcTemplate;
    private final String supportEmail;
    private final String websiteUrl;
    private final String platformName;

    public KnowledgeService(
            JdbcTemplate jdbcTemplate,
            @Value("${support.email:devendraambalkar11@gmail.com}") String supportEmail,
            @Value("${platform.website:${server.servlet.context-path:}}") String websiteUrl,
            @Value("${platform.name:SEVENT-MS}") String platformName) {
        this.jdbcTemplate = jdbcTemplate;
        this.supportEmail = supportEmail;
        this.websiteUrl = websiteUrl;
        this.platformName = platformName;
    }

    public ProjectKnowledge loadKnowledge() {
        ProjectKnowledge knowledge = new ProjectKnowledge();
        knowledge.setFiles(readTextFiles());
        return knowledge;
    }

    public String queryKnowledge(String query, ProjectKnowledge knowledge, boolean isAdmin, boolean isAuthenticated) {
        String normalized = query.toLowerCase(Locale.ROOT);

        if (isGreeting(normalized)) {
            return greeting();
        }
        if (looksLikeEventSearch(normalized)) {
            return "I can help you find an event. Are you looking for music, workshops, seminars, or something else?";
        }
        if (normalized.contains("event") || normalized.contains("browse") || normalized.contains("available")) {
            return summarizeLiveEvents();
        }
        if (normalized.contains("login") || normalized.contains("register") || normalized.contains("sign up") || normalized.contains("signup")) {
            return loginAndRegister(isAuthenticated);
        }
        if (normalized.contains("policy") || normalized.contains("terms") || normalized.contains("refund") || normalized.contains("privacy")
                || normalized.contains("contract") || normalized.contains("cancellation")) {
            return policySummary();
        }
        if (normalized.contains("support") || normalized.contains("contact") || normalized.contains("developer") || normalized.contains("about")) {
            return contactInfo();
        }
        if (normalized.contains("admin") && !isAdmin) {
            return "This question is admin-specific. Please log in as an admin to access admin workflows.";
        }
        return summarizeKnowledge(query, knowledge);
    }

    public List<SourceReference> findReferences(String query, ProjectKnowledge knowledge) {
        return knowledge.getFiles().stream()
                .filter(file -> file.getContent().toLowerCase().contains(query.toLowerCase()))
                .map(file -> new SourceReference(file.getPath(), 1))
                .collect(Collectors.toList());
    }

    private String summarizeKnowledge(String query, ProjectKnowledge knowledge) {
        if (query.toLowerCase().contains("event")) {
            return "Events are modeled in the events table and managed through AdminController and UserController. " +
                    "Major events and sub-events are related by parent_event_id. Booking logic lives in UserController and ApiController.";
        }
        if (query.toLowerCase().contains("login") || query.toLowerCase().contains("auth")) {
            return "Authentication and authorization are implemented in AuthController, ApiAuthController, and TokenStore. Admin login uses admin.email and admin.password.hash.";
        }
        return "I found project knowledge in the backend controllers, SQL schema, and frontend auth scripts. Ask specifically about events, bookings, admin, or signup flows for more detail.";
    }

    private String greeting() {
        return "Hi there! Welcome to " + platformName + ". I can help you browse events, explain login and booking, show policy details, or share support info.\n"
                + "1. View available events\n"
                + "2. Login / register\n"
                + "3. View contract & policy details\n"
                + "4. Contact support / about the developers";
    }

    private String summarizeLiveEvents() {
        List<Map<String, Object>> rows = jdbcTemplate.query("SELECT name, date, location, guest_limit, current_guests, description "
                        + "FROM events ORDER BY date ASC, event_id ASC LIMIT 5",
                (ResultSet rs, int rowNum) -> {
                    Map<String, Object> event = new LinkedHashMap<>();
                    event.put("name", rs.getString("name"));
                    event.put("date", rs.getString("date"));
                    event.put("location", rs.getString("location"));
                    event.put("guestLimit", rs.getInt("guest_limit"));
                    event.put("currentGuests", rs.getInt("current_guests"));
                    event.put("description", rs.getString("description"));
                    return event;
                });

        if (rows.isEmpty()) {
            return "I couldn't find any live events right now. Please try again later or contact support at " + supportEmail + ".";
        }

        StringBuilder message = new StringBuilder("Here are a few upcoming events:\n");
        for (Map<String, Object> event : rows) {
            int seatsLeft = Math.max((int) event.get("guestLimit") - (int) event.get("currentGuests"), 0);
            message.append("- ")
                    .append(event.get("name"))
                    .append(" | Date: ").append(event.get("date"))
                    .append(" | Venue: ").append(event.get("location"))
                    .append(" | Seats left: ").append(seatsLeft)
                    .append('\n');
        }
        message.append("Which event would you like to know more about or book?");
        return message.toString();
    }

    private String loginAndRegister(boolean isAuthenticated) {
        if (isAuthenticated) {
            return "You are already signed in, so you can browse events and continue booking. If you want, I can help you find an event next.";
        }
        return "To reserve tickets, you'll first need to sign in. Once you're logged in, I'll guide you through booking.";
    }

    private String policySummary() {
            return "Here is a simple summary: terms cover platform use, privacy covers your booking data, and refunds or cancellations follow the event rules shown in the documentation page. If you want, I can point you to the full policy next.";
    }

    private String contactInfo() {
        String websitePart = websiteUrl == null || websiteUrl.isBlank() ? "the platform website" : websiteUrl;
        return "For help, contact " + supportEmail + ". You can also visit " + websitePart + " for the latest platform details. This platform is maintained by the SEVENT-MS development team led by Devendra Ambalkar.";
    }

    private boolean isGreeting(String normalizedQuery) {
        return normalizedQuery.contains("hello")
                || normalizedQuery.contains("hi")
                || normalizedQuery.contains("hey")
                || normalizedQuery.contains("good morning")
                || normalizedQuery.contains("good afternoon")
                || normalizedQuery.contains("good evening");
    }

    private boolean looksLikeEventSearch(String normalizedQuery) {
        return normalizedQuery.contains("i want to attend")
                || normalizedQuery.contains("i want to go")
                || normalizedQuery.contains("find an event")
                || normalizedQuery.contains("recommend")
                || normalizedQuery.contains("something this weekend")
                || normalizedQuery.contains("what should i attend");
    }

    private List<ProjectKnowledge.FileContent> readTextFiles() {
        List<ProjectKnowledge.FileContent> files = new ArrayList<>();
        List<String> patterns = List.of("src/main/java/**/*.java", "src/main/resources/**/*.sql", "src/main/webapp/**/*.jsp", "frontend/**/*.html", "frontend/js/**/*.js");
        for (String pattern : patterns) {
            try {
                Files.walk(PROJECT_ROOT)
                        .filter(path -> path.toString().endsWith(getExtension(pattern)))
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                String content = Files.readString(path);
                                files.add(new ProjectKnowledge.FileContent(PROJECT_ROOT.relativize(path).toString(), content));
                            } catch (IOException ignored) {
                            }
                        });
            } catch (IOException ignored) {
            }
        }
        return files;
    }

    private String getExtension(String pattern) {
        if (pattern.endsWith(".java")) return ".java";
        if (pattern.endsWith(".sql")) return ".sql";
        if (pattern.endsWith(".jsp")) return ".jsp";
        if (pattern.endsWith(".html")) return ".html";
        if (pattern.endsWith(".js")) return ".js";
        return "";
    }
}
