package com.example.dt7syntaxcheck.share;

public class ErrorLog {

    private int line;
    private int index;
    private String message;

    public ErrorLog(int line, int index, String message) {
        this.line = line;
        this.index = index;
        this.message = message;
    }

    public int getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }

    public String getMessage() {
        return message;
    }
}
