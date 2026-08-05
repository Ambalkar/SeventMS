package com.eventms.chatbot.model;

public class SourceReference {
    private final String file;
    private final int line;

    public SourceReference(String file, int line) {
        this.file = file;
        this.line = line;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }
}
