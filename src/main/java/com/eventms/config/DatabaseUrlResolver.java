package com.eventms.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public final class DatabaseUrlResolver {

    private DatabaseUrlResolver() {
    }

    public static Map<String, String> fromEnvironment(Map<String, String> env) {
        String databaseUrl = blankToNull(env.get("DATABASE_URL"));
        String datasourceUrl = blankToNull(env.get("SPRING_DATASOURCE_URL"));
        String rawUrl = databaseUrl != null ? databaseUrl : datasourceUrl;
        String source = databaseUrl != null ? "DATABASE_URL" : "SPRING_DATASOURCE_URL";

        Map<String, String> properties = new HashMap<>();
        if (rawUrl == null) {
            return properties;
        }

        if (rawUrl.startsWith("jdbc:")) {
            properties.put("url", normalizeJdbcPostgresUrl(rawUrl));
            properties.put("source", source);
            putIfPresent(properties, "username", env.get("SPRING_DATASOURCE_USERNAME"));
            putIfPresent(properties, "password", env.get("SPRING_DATASOURCE_PASSWORD"));
            return properties;
        }

        if (!rawUrl.startsWith("postgres://") && !rawUrl.startsWith("postgresql://")) {
            properties.put("url", rawUrl);
            properties.put("source", source);
            putIfPresent(properties, "username", env.get("SPRING_DATASOURCE_USERNAME"));
            putIfPresent(properties, "password", env.get("SPRING_DATASOURCE_PASSWORD"));
            return properties;
        }

        URI uri = URI.create(rawUrl);
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbcUrl.append(":").append(uri.getPort());
        }
        jdbcUrl.append(uri.getPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append("?").append(uri.getRawQuery());
        }

        properties.put("url", normalizeJdbcPostgresUrl(jdbcUrl.toString()));
        properties.put("source", source);
        if (uri.getUserInfo() != null) {
            String[] credentials = uri.getUserInfo().split(":", 2);
            properties.put("username", decode(credentials[0]));
            if (credentials.length > 1) {
                properties.put("password", decode(credentials[1]));
            }
        }

        putIfPresent(properties, "username", env.get("SPRING_DATASOURCE_USERNAME"));
        putIfPresent(properties, "password", env.get("SPRING_DATASOURCE_PASSWORD"));
        return properties;
    }

    private static void putIfPresent(Map<String, String> properties, String key, String value) {
        if (blankToNull(value) != null) {
            properties.put(key, value);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String normalizeJdbcPostgresUrl(String url) {
        if (!url.startsWith("jdbc:postgresql://") || isLocalOrInternalPostgresUrl(url) || hasQueryParameter(url, "sslmode")) {
            return url;
        }

        return url + (url.contains("?") ? "&" : "?") + "sslmode=require";
    }

    private static boolean isLocalOrInternalPostgresUrl(String url) {
        String host = extractHost(url);
        if (host == null || host.isBlank()) {
            return false;
        }
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)
                || !host.contains(".");
    }

    private static String extractHost(String jdbcUrl) {
        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            return uri.getHost();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private static boolean hasQueryParameter(String url, String parameterName) {
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return false;
        }

        String normalizedName = parameterName.toLowerCase(Locale.ROOT);
        String[] queryParts = url.substring(queryStart + 1).split("&");
        for (String queryPart : queryParts) {
            int valueStart = queryPart.indexOf('=');
            String name = valueStart >= 0 ? queryPart.substring(0, valueStart) : queryPart;
            if (name.toLowerCase(Locale.ROOT).equals(normalizedName)) {
                return true;
            }
        }
        return false;
    }
}
