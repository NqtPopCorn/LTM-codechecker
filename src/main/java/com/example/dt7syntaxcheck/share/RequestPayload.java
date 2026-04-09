package com.example.dt7syntaxcheck.share;

public class RequestPayload {

    private String sourceCode;
    private int languageId;
    private boolean isFormatOnly; // CỜ MỚI: true nếu chỉ muốn format, false nếu muốn chạy code

    // Constructor cũ (mặc định là false để chạy code bình thường)
    public RequestPayload(String sourceCode, int languageId) {
        this.sourceCode = sourceCode;
        this.languageId = languageId;
        this.isFormatOnly = false;
    }

    // Constructor mới dành riêng cho nút Format
    public RequestPayload(String sourceCode, int languageId, boolean isFormatOnly) {
        this.sourceCode = sourceCode;
        this.languageId = languageId;
        this.isFormatOnly = isFormatOnly;
    }

    // Getters
    public String getSourceCode() {
        return sourceCode;
    }

    public int getLanguageId() {
        return languageId;
    }

    public boolean isFormatOnly() {
        return isFormatOnly;
    }
}
