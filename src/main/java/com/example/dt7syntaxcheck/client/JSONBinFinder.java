package com.example.dt7syntaxcheck.client;

import okhttp3.*;

import java.net.Socket;

import org.json.JSONObject;

import com.example.dt7syntaxcheck.share.DiscoveryConfig;

public class JSONBinFinder {

    private static final long STALE_THRESHOLD_MS = 60 * 60 * 1000L; // 1h

    private final OkHttpClient client = new OkHttpClient();

    public String findServerAddress() throws Exception {
        JSONObject record = fetchRecord();

        String ip = record.getString("ip");
        int port = record.getInt("port");
        long time = record.getLong("time");

        checkStale(time);

        System.out.println("[JSONBin] ✓ Tìm thấy server: " + ip + ":" + port);
        return ip + ":" + port;
    }

    private JSONObject fetchRecord() throws Exception {
        Request request = new Request.Builder()
                .url(DiscoveryConfig.BIN_URL + "/latest")
                .get()
                .addHeader("X-Master-Key", DiscoveryConfig.API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("JSONBin lỗi HTTP: " + response.code());
            }

            String body = response.body().string();
            // Response dạng: { "record": { "ip": "...", "port": ..., ... } }
            return new JSONObject(body).getJSONObject("record");
        }
    }

    private void checkStale(long registeredTime) {
        long age = System.currentTimeMillis() - registeredTime;
        if (age > STALE_THRESHOLD_MS) {
            System.out.printf("[JSONBin] ⚠ Server đăng ký cách đây %.0f phút, có thể đã offline.%n",
                    age / 60000.0);
        }
    }
}