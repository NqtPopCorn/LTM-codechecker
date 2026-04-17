package com.example.dt7syntaxcheck.share;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * HybridCryptoManager: Mã hóa lai kết hợp RSA (Asymmetric) + AES (Symmetric)
 *
 * Quá trình: 1. Client tạo session key AES ngẫu nhiên 2. Client mã hóa session
 * key bằng RSA public key của server 3. Client mã hóa dữ liệu bằng AES session
 * key 4. Server giải mã session key bằng RSA private key 5. Server giải mã dữ
 * liệu bằng AES session key
 */
public class HybridCryptoManager {

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String AES_ALGORITHM = "AES";
    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256; // 256-bit AES key

    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * Constructor cho Client: Chỉ cần public key từ server
     */
    public HybridCryptoManager(String base64PublicKey) {
        try {
            this.publicKey = loadPublicKey(base64PublicKey);
            this.privateKey = null;
        } catch (Exception e) {
            System.err.println("Lỗi load public key: " + e.getMessage());
        }
    }

    /**
     * Constructor cho Server: Cần cả public key và private key
     */
    public HybridCryptoManager(String base64PublicKey, String base64PrivateKey) {
        try {
            this.publicKey = loadPublicKey(base64PublicKey);
            this.privateKey = loadPrivateKey(base64PrivateKey);
        } catch (Exception e) {
            System.err.println("Lỗi load keys: " + e.getMessage());
        }
    }

    /**
     * Tải public key từ chuỗi Base64
     */
    private PublicKey loadPublicKey(String base64Key) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /**
     * Tải private key từ chuỗi Base64
     */
    private PrivateKey loadPrivateKey(String base64Key) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Tạo session key AES ngẫu nhiên (256-bit)
     */
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    /**
     * Mã hóa session key bằng RSA public key
     */
    public String encryptSessionKey(SecretKey sessionKey) throws Exception {
        if (publicKey == null) {
            throw new Exception("Public key chưa được khởi tạo!");
        }
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = cipher.doFinal(sessionKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    /**
     * Giải mã session key bằng RSA private key
     */
    public SecretKey decryptSessionKey(String encryptedSessionKey) throws Exception {
        if (privateKey == null) {
            throw new Exception("Private key chưa được khởi tạo!");
        }
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decodedKey = Base64.getDecoder().decode(encryptedSessionKey);
        byte[] decryptedKey = cipher.doFinal(decodedKey);
        return new SecretKeySpec(decryptedKey, 0, decryptedKey.length, AES_ALGORITHM);
    }

    /**
     * Mã hóa dữ liệu bằng AES
     */
    public String encryptDataWithAES(String data, SecretKey sessionKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Giải mã dữ liệu bằng AES
     */
    public String decryptDataWithAES(String encryptedData, SecretKey sessionKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sessionKey);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedData);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Mã hóa toàn bộ (Hybrid): Tạo session key AES -> Mã hóa data + key Phiên
     * bản mở rộng: Trả về session key plaintext để client có thể tái sử dụng
     */
    public HybridEncryptedMessageWithKey encryptHybridWithKey(String plaintext) throws Exception {
        SecretKey sessionKey = generateAESKey();
        String encryptedSessionKey = encryptSessionKey(sessionKey);
        String encryptedData = encryptDataWithAES(plaintext, sessionKey);
        return new HybridEncryptedMessageWithKey(encryptedSessionKey, encryptedData, sessionKey);
    }

    /**
     * Mã hóa toàn bộ (Hybrid): Tạo session key AES -> Mã hóa data + key
     */
    public HybridEncryptedMessage encryptHybrid(String plaintext) throws Exception {
        SecretKey sessionKey = generateAESKey();
        String encryptedSessionKey = encryptSessionKey(sessionKey);
        String encryptedData = encryptDataWithAES(plaintext, sessionKey);
        return new HybridEncryptedMessage(encryptedSessionKey, encryptedData);
    }

    /**
     * Giải mã toàn bộ (Hybrid): Giải mã key -> Giải mã data
     */
    public String decryptHybrid(HybridEncryptedMessage message) throws Exception {
        SecretKey sessionKey = decryptSessionKey(message.getEncryptedSessionKey());
        return decryptDataWithAES(message.getEncryptedData(), sessionKey);
    }

    /**
     * Lớp lưu trữ tin nhắn mã hóa lai (kèm session key cho phía gửi)
     */
    public static class HybridEncryptedMessageWithKey {

        private String encryptedSessionKey;  // Session key đã mã hóa bằng RSA
        private String encryptedData;        // Dữ liệu đã mã hóa bằng AES
        private javax.crypto.SecretKey sessionKey;     // Session key (plaintext, lưu ở client)

        public HybridEncryptedMessageWithKey(String encryptedSessionKey, String encryptedData, javax.crypto.SecretKey sessionKey) {
            this.encryptedSessionKey = encryptedSessionKey;
            this.encryptedData = encryptedData;
            this.sessionKey = sessionKey;
        }

        public String getEncryptedSessionKey() {
            return encryptedSessionKey;
        }

        public String getEncryptedData() {
            return encryptedData;
        }

        public javax.crypto.SecretKey getSessionKey() {
            return sessionKey;
        }
    }

    /**
     * Lớp lưu trữ tin nhắn mã hóa lai
     */
    public static class HybridEncryptedMessage {

        private String encryptedSessionKey;  // Session key đã mã hóa bằng RSA
        private String encryptedData;        // Dữ liệu đã mã hóa bằng AES

        public HybridEncryptedMessage(String encryptedSessionKey, String encryptedData) {
            this.encryptedSessionKey = encryptedSessionKey;
            this.encryptedData = encryptedData;
        }

        public String getEncryptedSessionKey() {
            return encryptedSessionKey;
        }

        public String getEncryptedData() {
            return encryptedData;
        }
    }
}
