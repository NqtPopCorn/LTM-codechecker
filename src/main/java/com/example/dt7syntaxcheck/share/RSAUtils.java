package com.example.dt7syntaxcheck.share;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

public class RSAUtils {

    private static String readResource(String fileName) throws Exception {
        try (InputStream is = RSAUtils.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null)
                throw new IllegalArgumentException("Không tìm thấy file: " + fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining());
            }
        }
    }

    public static PublicKey getPublicKey(String path) throws Exception {
        String key = readResource(path);
        byte[] byteKey = Base64.getDecoder().decode(key);
        X509EncodedKeySpec x509diff = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(x509diff);
    }

    public static PrivateKey getPrivateKey(String path) throws Exception {
        String key = readResource(path);
        byte[] byteKey = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(pkcs8);
    }

    public static void main(String[] args) {
        try {
            // 1. Gen Key như cũ
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            String publicKeyString = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            String privateKeyString = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

            Path resourcePath = Paths.get("src", "main", "resources", "keys");

            // 3. Tạo thư mục nếu chưa có
            if (Files.notExists(resourcePath)) {
                Files.createDirectories(resourcePath);
            }

            // 4. Ghi file
            Files.writeString(resourcePath.resolve("public.key"), publicKeyString);
            Files.writeString(resourcePath.resolve("private.key"), privateKeyString);

            // Base64.getEncoder().encodegetPrivateKey("keys/private.key").getEncoded();

            System.out.println("Đã lưu khóa vào: " + resourcePath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}