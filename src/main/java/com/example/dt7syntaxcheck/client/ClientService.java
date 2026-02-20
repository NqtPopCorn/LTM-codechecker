package com.example.dt7syntaxcheck.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

// class này xử lý kết nối và giao tiếp với server socket
// bằng cách tạo các thread (worker) để xử lý mà không blocking UI(EDT thread)
public class ClientService {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private MessageListener listener;

    public ClientService(MessageListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                startReceiving();

            } catch (IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onDisconnected();
                }
            }
        }).start();
    }

    private void startReceiving() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (listener != null) {
                        listener.onMessageReceived(msg);
                    }
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.onDisconnected();
                }
            }
        }).start();
    }

    public void send(String message) {
        if (out != null) {
            try {
                out.write(message);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
