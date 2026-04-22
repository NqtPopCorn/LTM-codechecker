package com.example.dt7syntaxcheck.server.services;

import okhttp3.*;
import org.json.JSONObject;

import com.example.dt7syntaxcheck.share.DiscoveryConfig;

public class JSONBinRegistrar {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    public void register(int tcpPort) throws Exception {
        String publicIP = fetchPublicIP();

        String body = new JSONObject()
                .put("app", DiscoveryConfig.APP_NAME)
                .put("ip", publicIP)
                .put("port", tcpPort)
                .put("time", System.currentTimeMillis())
                .toString();

        Request request = new Request.Builder()
                .url(DiscoveryConfig.BIN_URL)
                .put(RequestBody.create(body, JSON))
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Master-Key", DiscoveryConfig.API_KEY)
                .addHeader("X-Bin-Versioning", "false")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("[JSONBin] ✓ Đăng ký thành công: " + publicIP + ":" + tcpPort);
            } else {
                System.err.println("[JSONBin] ✗ Lỗi HTTP: " + response.code() + " - " + response.message());
            }
        }
    }

    private String fetchPublicIP() throws Exception {
        Request request = new Request.Builder()
                .url("https://api.ipify.org")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new Exception("Không lấy được public IP");
            return response.body().string().trim();
        }
    }
}