package com.eventms.chatbot;

import com.eventms.chatbot.model.ProjectKnowledge;
import com.eventms.chatbot.model.SourceReference;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {

    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));

    public ProjectKnowledge loadKnowledge() {
        ProjectKnowledge knowledge = new ProjectKnowledge();
        knowledge.setFiles(readTextFiles());
        return knowledge;
    }

    public String queryKnowledge(String query, ProjectKnowledge knowledge, boolean isAdmin) {
        if (query.toLowerCase().contains("admin") && !isAdmin) {
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
