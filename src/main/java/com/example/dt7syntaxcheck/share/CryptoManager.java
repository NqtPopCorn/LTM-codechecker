package com.example.dt7syntaxcheck.share;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoManager {

    private static final String ALGORITHM = "AES";
    private SecretKeySpec secretKey;

    /**
     * Constructor khởi tạo bộ mã hóa/giải mã.
     *
     * @param myKey: Chuỗi mật khẩu bất kỳ. Hệ thống sẽ tự động băm (hash) chuỗi
     *               này để tạo ra một Key chuẩn 16-byte cho thuật toán AES.
     */
    public CryptoManager(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key); // hash ra để tăng hỗn lại, đạt độ dài
            key = Arrays.copyOf(key, 16); // AES-128 yêu cầu key dài đúng 16 byte
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            System.err.println("Lỗi tạo SecretKey: " + e.getMessage());
        }
    }

    /**
     * Hàm mã hóa (Encrypt): Chuyển chuỗi JSON gốc thành chuỗi đã mã hóa
     */
    public String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            // Trả về chuỗi Base64 để truyền đi an toàn qua Socket dạng Text
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.err.println("Lỗi khi mã hóa: " + e.toString());
        }
        return null;
    }

    /**
     * Hàm giải mã (Decrypt): Chuyển chuỗi mã hóa nhận từ Server về lại chuỗi
     * JSON gốc
     */
    public String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Lỗi khi giải mã: " + e.toString());
        }
        return null;
    }
}
