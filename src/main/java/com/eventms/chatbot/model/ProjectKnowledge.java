package com.eventms.chatbot.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectKnowledge {
    private List<FileContent> files = new ArrayList<>();

    public List<FileContent> getFiles() {
        return files;
    }

    public void setFiles(List<FileContent> files) {
        this.files = files;
    }

    public static class FileContent {
        private final String path;
        private final String content;

        public FileContent(String path, String content) {
            this.path = path;
            this.content = content;
        }

        public String getPath() {
            return path;
        }

        public String getContent() {
            return content;
        }
    }
}
