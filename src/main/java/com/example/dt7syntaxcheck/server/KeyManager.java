package com.example.dt7syntaxcheck.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * KeyManager: Quản lý tạo, lưu trữ, và tải RSA key pairs cho server
 *
 * Các key được lưu trong file để tái sử dụng: - public_key.txt (công khai, gửi
 * cho client) - private_key.txt (bí mật, lưu trên server)
 */
public class KeyManager {

    private static final String PUBLIC_KEY_FILE = "public_key.txt";
    private static final String PRIVATE_KEY_FILE = "private_key.txt";
    private static final int RSA_KEY_SIZE = 2048;

    /**
     * Tạo cặp RSA key pair mới (2048-bit)
     */
    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    /**
     * Lưu public key vào file
     */
    public static void savePublicKey(PublicKey publicKey) throws Exception {
        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        try (FileWriter writer = new FileWriter(PUBLIC_KEY_FILE)) {
            writer.write(encodedKey);
        }
        System.out.println("[+] Public key đã lưu vào: " + PUBLIC_KEY_FILE);
    }

    /**
     * Lưu private key vào file
     */
    public static void savePrivateKey(PrivateKey privateKey) throws Exception {
        String encodedKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        try (FileWriter writer = new FileWriter(PRIVATE_KEY_FILE)) {
            writer.write(encodedKey);
        }
        System.out.println("[+] Private key đã lưu vào: " + PRIVATE_KEY_FILE);
    }

    /**
     * Tải public key từ file
     */
    public static String loadPublicKey() throws Exception {
        File file = new File(PUBLIC_KEY_FILE);
        if (!file.exists()) {
            throw new Exception("File " + PUBLIC_KEY_FILE + " không tồn tại!");
        }
        StringBuilder key = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            int character;
            while ((character = reader.read()) != -1) {
                key.append((char) character);
            }
        }
        return key.toString();
    }

    /**
     * Tải private key từ file
     */
    public static String loadPrivateKey() throws Exception {
        File file = new File(PRIVATE_KEY_FILE);
        if (!file.exists()) {
            throw new Exception("File " + PRIVATE_KEY_FILE + " không tồn tại!");
        }
        StringBuilder key = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            int character;
            while ((character = reader.read()) != -1) {
                key.append((char) character);
            }
        }
        return key.toString();
    }

    /**
     * Kiểm tra xem key files có tồn tại không
     */
    public static boolean keyFilesExist() {
        return new File(PUBLIC_KEY_FILE).exists() && new File(PRIVATE_KEY_FILE).exists();
    }

    /**
     * Tạo hoặc tải RSA key pairs
     */
    public static RSAKeyPair initializeKeys() throws Exception {
        if (keyFilesExist()) {
            System.out.println("[+] Tải RSA keys từ file...");
            String publicKeyStr = loadPublicKey();
            String privateKeyStr = loadPrivateKey();
            return new RSAKeyPair(publicKeyStr, privateKeyStr);
        } else {
            System.out.println("[+] Tạo cặp RSA keys mới...");
            KeyPair keyPair = generateRSAKeyPair();
            savePublicKey(keyPair.getPublic());
            savePrivateKey(keyPair.getPrivate());
            String publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            return new RSAKeyPair(publicKeyStr, privateKeyStr);
        }
    }

    /**
     * Lớp lưu trữ cặp public/private key dưới dạng Base64
     */
    public static class RSAKeyPair {

        public String publicKey;
        public String privateKey;

        public RSAKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }
}
