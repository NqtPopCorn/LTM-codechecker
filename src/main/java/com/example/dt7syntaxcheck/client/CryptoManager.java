package com.example.dt7syntaxcheck.client;
//file này xử lý mã hóa và giải mã dữ liệu giữa client và server
public interface CryptoManager {
    void onMessageReceived(String message);

    void onDisconnected();
}
