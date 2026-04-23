package com.example.dt7syntaxcheck.share;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ServiceRegistry: Quản lý đăng ký server IP lên GitHub Gist - Server sẽ gọi
 * registerServer() để gửi IP của nó lên GitHub Gist - Client sẽ gọi
 * discoverServer() để lấy IP của server từ GitHub Gist
 */
public class ServiceRegistry {

    // =====================================================
    // CẤU HÌNH CHO GITHUB GIST
    // =====================================================
    // Gist ID - ID của Gist trên GitHub (sẽ được tạo lần đầu)
    // BẠN CẦN THAY ĐỔI: Tạo 1 Gist mới trên GitHub, lấy ID từ URL
    // VÍ DỤ: https://gist.github.com/username/GIST_ID
    private static final String GIST_ID = "cbc74a1a6b6d3a24ce85ea1e1cdf3915";  // Thay thế bằng ID thực tế

    // GitHub Personal Access Token
    // BẠN CẦN THAY ĐỔI: Tạo token trên GitHub Settings -> Developer settings -> Personal access tokens
    // Token cần quyền: gist (create, read, update gists)
    private static final String GITHUB_TOKEN = "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";  // Thay thế bằng token thực tế

    // Tên file trong Gist để lưu trữ server IP
    private static final String GIST_FILENAME = "server_registry.txt";

    // GitHub API endpoints
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    // =====================================================
    // PHƯƠNG THỨC: LẤY IP ĐỊA CHỈ CỦA MÁY HIỆN TẠI
    // =====================================================
    /**
     * Lấy IP address của máy hiện tại
     *
     * @return IP address dạng string (ví dụ: 192.168.1.100)
     */
    public static String getLocalIPAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("[ERROR] Không thể lấy IP address: " + e.getMessage());
            return "127.0.0.1";  // Fallback
        }
    }

    // =====================================================
    // PHƯƠNG THỨC: ĐĂNG KÝ SERVER (Gọi từ ServerMain)
    // =====================================================
    /**
     * Đăng ký IP của server lên GitHub Gist
     *
     * @param serverPort cổng mà server đang lắng nghe
     * @return true nếu đăng ký thành công, false nếu thất bại
     */
    public static boolean registerServer(int serverPort) {
        if (GIST_ID.equals("YOUR_GIST_ID_HERE") || GITHUB_TOKEN.equals("YOUR_GITHUB_TOKEN_HERE")) {
            System.err.println("[!] CẢNH BÁO: Cần cấu hình GitHub Gist ID và Token");
            System.err.println("[!] Hãy thay đổi GIST_ID và GITHUB_TOKEN trong ServiceRegistry.java");
            return false;
        }

        try {
            String localIP = getLocalIPAddress();
            String serverInfo = String.format("%s:%d", localIP, serverPort);

            System.out.println("[INFO] Đang đăng ký server IP: " + serverInfo);

            // Tạo JSON object chứa thông tin server
            JSONObject gistJson = new JSONObject();
            JSONObject files = new JSONObject();
            JSONObject fileContent = new JSONObject();

            fileContent.put("content", serverInfo);
            files.put(GIST_FILENAME, fileContent);
            gistJson.put("files", files);
            gistJson.put("description", "Server IP Registry");

            // Gửi PATCH request tới GitHub API để cập nhật Gist
            String url = GITHUB_API_URL + "/gists/" + GIST_ID;
            RequestBody body = RequestBody.create(
                    gistJson.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .patch(body)
                    .addHeader("Authorization", "Bearer " + GITHUB_TOKEN)
                    .addHeader("Accept", "application/vnd.github+json")
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("[+] Đăng ký server thành công!");
                    System.out.println("[+] Server IP đã được lưu tại GitHub Gist: " + serverInfo);
                    return true;
                } else {
                    System.err.println("[-] Lỗi đăng ký server (Code: " + response.code() + ")");
                    System.err.println("[-] Response: " + response.body().string());
                    return false;
                }
            }

        } catch (Exception e) {
            System.err.println("[-] Lỗi khi đăng ký server: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // PHƯƠNG THỨC: KHÁM PHÁ SERVER (Gọi từ ClientService)
    // =====================================================
    /**
     * Lấy IP của server từ GitHub Gist
     *
     * @return Server IP dạng string (ví dụ: 192.168.1.100:5000), hoặc null nếu
     * thất bại
     */
    public static String discoverServer() {
        if (GIST_ID.equals("YOUR_GIST_ID_HERE") || GITHUB_TOKEN.equals("YOUR_GITHUB_TOKEN_HERE")) {
            System.err.println("[!] CẢNH BÁO: Cần cấu hình GitHub Gist ID và Token");
            System.err.println("[!] Hãy thay đổi GIST_ID và GITHUB_TOKEN trong ServiceRegistry.java");
            return null;
        }

        try {
            System.out.println("[INFO] Đang khám phá server từ GitHub Gist...");

            // Gửi GET request tới GitHub API để lấy Gist
            String url = GITHUB_API_URL + "/gists/" + GIST_ID;
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + GITHUB_TOKEN)
                    .addHeader("Accept", "application/vnd.github+json")
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    JSONObject gistObject = new JSONObject(responseBody);
                    JSONObject files = gistObject.getJSONObject("files");

                    if (files.has(GIST_FILENAME)) {
                        String serverIP = files.getJSONObject(GIST_FILENAME).getString("content");
                        System.out.println("[+] Khám phá server thành công!");
                        System.out.println("[+] Server IP: " + serverIP);
                        return serverIP;
                    } else {
                        System.err.println("[-] Không tìm thấy file registry trong Gist");
                        return null;
                    }
                } else {
                    System.err.println("[-] Lỗi khám phá server (Code: " + response.code() + ")");
                    return null;
                }
            }

        } catch (Exception e) {
            System.err.println("[-] Lỗi khi khám phá server: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // =====================================================
    // PHƯƠNG THỨC: HỖ TRỢ - TẠO GIST MỚI (KHỞI TẠO)
    // =====================================================
    /**
     * Tạo một Gist mới trên GitHub nếu chưa tồn tại Gọi lần đầu tiên để khởi
     * tạo registry
     *
     * @return Gist ID của gist mới được tạo
     */
    public static String createNewGist() {
        if (GITHUB_TOKEN.equals("YOUR_GITHUB_TOKEN_HERE")) {
            System.err.println("[!] CẢNH BÁO: Cần cấu hình GitHub Token");
            return null;
        }

        try {
            System.out.println("[INFO] Đang tạo Gist mới...");

            JSONObject gistJson = new JSONObject();
            JSONObject files = new JSONObject();
            JSONObject fileContent = new JSONObject();

            fileContent.put("content", "Server IP Registry - Initialized");
            files.put(GIST_FILENAME, fileContent);

            gistJson.put("files", files);
            gistJson.put("description", "Server IP Registry for LTM-CodeChecker");
            gistJson.put("public", false);  // Private gist

            RequestBody body = RequestBody.create(
                    gistJson.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(GITHUB_API_URL + "/gists")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + GITHUB_TOKEN)
                    .addHeader("Accept", "application/vnd.github+json")
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    JSONObject gistObject = new JSONObject(responseBody);
                    String gistId = gistObject.getString("id");
                    System.out.println("[+] Gist mới được tạo thành công!");
                    System.out.println("[+] Gist ID: " + gistId);
                    System.out.println("[+] Hãy cập nhật GIST_ID trong ServiceRegistry.java bằng ID này");
                    return gistId;
                } else {
                    System.err.println("[-] Lỗi tạo Gist (Code: " + response.code() + ")");
                    System.err.println("[-] Response: " + response.body().string());
                    return null;
                }
            }

        } catch (Exception e) {
            System.err.println("[-] Lỗi khi tạo Gist: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
