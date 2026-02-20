package com.example.dt7syntaxcheck.client;

public interface MessageListener {
    void onMessageReceived(String message);

    void onDisconnected();
}
