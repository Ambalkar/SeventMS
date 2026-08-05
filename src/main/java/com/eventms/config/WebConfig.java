package com.eventms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String EVENT_IMAGE_PATH = "uploads/images/events";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(System.getProperty("user.dir"), EVENT_IMAGE_PATH);
        String uploadPath = uploadDir.toUri().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }
        registry.addResourceHandler("/images/events/**")
                .addResourceLocations(uploadPath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        registry.addMapping("/chatbot/**")
                .allowedOriginPatterns("*")
                .allowedMethods("POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
