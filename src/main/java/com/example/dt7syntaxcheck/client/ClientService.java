package com.example.dt7syntaxcheck.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.dt7syntaxcheck.share.CryptoManager;
import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.google.gson.Gson;

public class ClientService {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;

    private CryptoManager cryptoManager;
    private Gson gson;

    public ClientService() {
        this.gson = new Gson();
        // Giả sử dùng chung 1 key tạm thời để test. 
        // Sau này có thể làm chức năng trao đổi key RSA với Server khi vừa kết nối.
        this.cryptoManager = new CryptoManager("MySecretKey123456");
    }

    // Hàm này nhận payload, mã hóa, gửi đi, chờ nhận về và giải mã
    public ResponsePayload sendCodeToServer(RequestPayload requestPayload) throws Exception {
        // 1. Biến đối tượng thành chuỗi JSON
        String jsonPayload = gson.toJson(requestPayload);

        // 2. Mã hóa chuỗi JSON
        String encryptedData = cryptoManager.encrypt(jsonPayload);

        // 3. Gửi qua Socket 
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Gửi dữ liệu đã mã hóa lên Server
            out.println(encryptedData);

            // 4. Chờ nhận phản hồi từ Server
            String encryptedResponse = in.readLine();

            if (encryptedResponse == null) {
                throw new Exception("Mất kết nối với Server!");
            }

            // 5. Giải mã phản hồi
            String decryptedJson = cryptoManager.decrypt(encryptedResponse);

            // 6. Biến chuỗi JSON trở lại thành đối tượng ResponsePayload
            return gson.fromJson(decryptedJson, ResponsePayload.class);
        }
    }
}
