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
        // Serve uploaded event images from the filesystem and enable a short cache period
        registry.addResourceHandler("/images/events/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600); // cache for 1 hour (seconds)
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow CORS for API endpoints
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        // Also allow cross-origin requests for static image resources (frontend on Vercel will request images from backend)
        registry.addMapping("/images/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
