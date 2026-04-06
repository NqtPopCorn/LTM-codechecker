package com.example.dt7syntaxcheck.share;

public class RequestPayload {

    private String sourceCode;
    private int languageId; // ID ngôn ngữ (vd: 71 cho Python, 62 cho Java...)

    // Constructor
    public RequestPayload(String sourceCode, int languageId) {
        this.sourceCode = sourceCode;
        this.languageId = languageId;
    }

    // Getters
    public String getSourceCode() {
        return sourceCode;
    }

    public int getLanguageId() {
        return languageId;
    }
}
