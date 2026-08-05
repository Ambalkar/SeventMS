package com.eventms.chatbot;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class ChatbotTemplateService {
    private final Properties templates = loadTemplates();

    public String get(String key) {
        return templates.getProperty(key, "");
    }

    private Properties loadTemplates() {
        Properties properties = new Properties();
        ClassPathResource resource = new ClassPathResource("chatbot-templates.properties");
        try (InputStream inputStream = resource.getInputStream()) {
            properties.load(inputStream);
        } catch (IOException ignored) {
        }
        return properties;
    }
}
