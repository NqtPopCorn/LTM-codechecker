# Mã Hóa Lai (Hybrid Encryption) - Tài Liệu Kỹ Thuật

## 📌 Tổng Quan

Dự án LTM Code Checker đã được nâng cấp để sử dụng **Mã hóa Lai (Hybrid Encryption)** - một phương pháp kết hợp **RSA (Asymmetric Encryption)** và **AES (Symmetric Encryption)** để đạt được bảo mật cao nhất.

---

## 🔐 Khái Niệm Cơ Bản

### Mã Hóa Đối Xứng (Symmetric Encryption)
- **Thuật toán**: AES (Advanced Encryption Standard) - 256-bit
- **Tốc độ**: Rất nhanh
- **Hạn chế**: Cả 2 bên phải biết chung một key (khó trao đổi key an toàn)
- **Sử dụng**: Mã hóa dữ liệu lớn

### Mã Hóa Bất Đối Xứng (Asymmetric Encryption)
- **Thuật toán**: RSA 2048-bit
- **Tốc độ**: Chậm hơn AES
- **Ưu điểm**: Sử dụng cặp public/private key (public key không bí mật)
- **Sử dụng**: Trao đổi session key

### Mã Hóa Lai (Hybrid Encryption)
- Kết hợp RSA + AES để lấy **tốc độ cao + bảo mật cao**
- Giải quyết vấn đề trao đổi key an toàn
- **Best practice** cho các ứng dụng yêu cầu bảo mật

---

## 🏗️ Kiến Trúc Mã Hóa Lai

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 1. Nhận public key từ server (Key Exchange)              │  │
│  │    → Tạo HybridCryptoManager(publicKey)                  │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 2. Mã Hóa Hybrid:                                          │  │
│  │    a) Tạo Session Key AES ngẫu nhiên (256-bit)          │  │
│  │    b) Mã hóa Session Key bằng RSA public key             │  │
│  │    c) Mã hóa dữ liệu JSON bằng AES session key           │  │
│  │    d) Gửi: (encrypted session key, encrypted data)      │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                    (TCP Socket Port 5000)
                              │
┌─────────────────────────────────────────────────────────────────┐
│                    SERVER                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 1. Gửi public key cho client (Key Exchange)              │  │
│  │    → Cấu hình HybridCryptoManager(public, private)       │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 2. Giải Mã Hybrid:                                         │  │
│  │    a) Nhận: (encrypted session key, encrypted data)      │  │
│  │    b) Giải mã Session Key bằng RSA private key           │  │
│  │    c) Giải mã dữ liệu JSON bằng AES session key          │  │
│  │    d) Xử lý yêu cầu                                      │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 3. Mã Hóa Response:                                        │  │
│  │    a) Tạo Session Key AES mới ngẫu nhiên                 │  │
│  │    b) Mã hóa response bằng RSA + AES (hybrid)            │  │
│  │    c) Gửi: (encrypted session key, encrypted data)      │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                    (TCP Socket Port 5000)
                              │
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 4. Giải Mã Response:                                      │  │
│  │    a) Nhận: (encrypted session key, encrypted data)      │  │
│  │    b) Giải mã Session Key bằng RSA private key           │  │
│  │    c) Giải mã dữ liệu bằng AES session key               │  │
│  │    d) Hiển thị kết quả                                   │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Quy Trình Chi Tiết

### Máy Chủ (Server)

#### Khởi Động
1. `ServerMain.main()` khởi động
2. Gọi `KeyManager.initializeKeys()`
   - Kiểm tra xem files `public_key.txt` và `private_key.txt` có tồn tại
   - Nếu không: Tạo cặp RSA 2048-bit mới và lưu vào file
   - Nếu có: Tải từ file (tái sử dụng key cũ)
3. Lưu RSA key pair vào `serverMain.rsaKeyPair`
4. Khởi tạo `ServerSocket` lắng nghe tại port 5000

#### Xử Lý Client
1. Nhận kết nối từ client
2. Tạo luồng `ClientHandler` mới, truyền `rsaKeyPair`
3. Trong `ClientHandler.run()`:
   - **Bước 1**: Gửi `public_key` cho client
   - **Bước 2**: Nhận 2 dòng dữ liệu: `encryptedSessionKey` + `encryptedData`
   - **Bước 3**: Tạo `HybridCryptoManager` với public key + private key
   - **Bước 4**: Gọi `decryptHybrid()` để giải mã dữ liệu
   - **Bước 5**: Xử lý yêu cầu (kiểm tra cú pháp, chạy code, v.v.)
   - **Bước 6**: Mã hóa response bằng `encryptHybrid()`
   - **Bước 7**: Gửi 2 dòng: `encryptedSessionKey` + `encryptedData` về client

### Máy Khách (Client)

#### Kết Nối
1. Mở `Socket` kết nối tới server (localhost:5000)
2. Tạo `BufferedReader` + `PrintWriter` để giao tiếp
3. **Bước 1 - Key Exchange**: Đọc 1 dòng → lấy `public_key` từ server
4. Tạo `HybridCryptoManager` chỉ với `public_key`

#### Gửi Yêu Cầu
1. Chuyển `RequestPayload` thành JSON
2. Gọi `encryptHybrid(json)`:
   - Tạo session key AES 256-bit ngẫu nhiên
   - Mã hóa session key bằng RSA public key
   - Mã hóa JSON bằng AES session key
   - Trả về `HybridEncryptedMessage(encryptedKey, encryptedData)`
3. Gửi 2 dòng:
   - `encryptedSessionKey`
   - `encryptedData`

#### Nhận Response
1. Đọc 2 dòng: `encryptedSessionKey` + `encryptedData`
2. Tạo `HybridEncryptedMessage` từ 2 dòng
3. Gọi `decryptHybrid(message)`:
   - Giải mã session key bằng RSA private key
   - Giải mã dữ liệu bằng AES session key
   - Trả về JSON plain text
4. Chuyển JSON thành `ResponsePayload` object
5. Hiển thị kết quả trên GUI

---

## 📁 File Mới / Cập Nhật

### File Mới
1. **`HybridCryptoManager.java`**
   - Xử lý mã hóa/giải mã hybrid (RSA + AES)
   - Constructor cho client (chỉ public key)
   - Constructor cho server (public + private key)

2. **`HybridCryptoManager.HybridEncryptedMessage` (Inner Class)**
   - Lưu trữ `encryptedSessionKey` + `encryptedData`

### File Cập Nhật
1. **`KeyManager.java`** (cũ rỗng)
   - Tạo cặp RSA key pair
   - Lưu/tải key từ file
   - Kiểm tra xem key files có tồn tại

2. **`ServerMain.java`**
   - Khởi tạo RSA keys trước khi lắng nghe
   - Truyền `rsaKeyPair` cho mỗi `ClientHandler`

3. **`ClientHandler.java`**
   - Constructor nhận `rsaKeyPair`
   - Thực hiện key exchange (gửi public key)
   - Giải mã hybrid message từ client
   - Mã hóa hybrid response gửi về client

4. **`ClientService.java`**
   - Nhận public key từ server trong key exchange
   - Khởi tạo `HybridCryptoManager` chỉ với public key
   - Mã hóa hybrid request
   - Giải mã hybrid response

---

## 🔑 Cấu Trúc Key Files

### `public_key.txt`
```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2a5v9s2X1b...
(Base64-encoded X.509 SubjectPublicKeyInfo format)
```

### `private_key.txt`
```
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDZrm/2zZfVvRc...
(Base64-encoded PKCS#8 PrivateKeyInfo format)
```

---

## 🛡️ Lợi Ích Bảo Mật

### So Sánh Trước và Sau

| Tiêu chí | AES Đơn (Cũ) | RSA + AES Hybrid (Mới) |
|----------|---------------|------------------------|
| **Key Exchange** | Cùng key cố định | RSA public/private |
| **Session Key** | Không có | Mới tạo mỗi lần |
| **Tốc độ** | Nhanh | Nhanh (AES) + An toàn (RSA) |
| **Tái sử dụng Key** | Có nguy hiểm | Không (key mới mỗi lần) |
| **Độ phức tạp RSA** | N/A | 2048-bit (rất mạnh) |

---

## 🚀 Hướng Dẫn Sử Dụng

### Khởi Động Server
```bash
# Lần đầu: Tạo RSA keys
java -cp target/dt7syntaxcheck-1.0-SNAPSHOT.jar com.example.dt7syntaxcheck.server.ServerMain

# Output:
# [+] Tạo cặp RSA keys mới...
# [+] Public key đã lưu vào: public_key.txt
# [+] Private key đã lưu vào: private_key.txt
# [+] RSA keys đã khởi tạo thành công!
# [INFO] Server đang lắng nghe kết nối tại port 5000...
```

### Khởi Động Client
```bash
java -cp target/dt7syntaxcheck-1.0-SNAPSHOT.jar com.example.dt7syntaxcheck.client.ClientUIFrame

# Output (console):
# [CLIENT] ✓ Nhận public key từ server (Key Exchange thành công!)
# [CLIENT] ✓ Mã hóa Hybrid thành công!
# [CLIENT] → Session Key (RSA): MIIBIjANBgkqhkiG9w0BIQ...
# [CLIENT] → Data (AES): 8P9L2x4q7R3nM5vW1Y8zQ...
# [CLIENT] ✓ Đã gửi dữ liệu mã hóa tới server
# [CLIENT] ✓ Nhận response mã hóa từ server
# [CLIENT] ✓ Giải mã Hybrid thành công!
```

### Giao Diện GUI Client
1. Viết code vào editor
2. Chọn ngôn ngữ từ combo box
3. Click nút **"Check"** hoặc **"Format"**
4. **Tự động**: Dữ liệu được mã hóa hybrid, gửi đi, nhận về, giải mã
5. Kết quả hiển thị trên console output

---

## 🔧 Tuỳ Chỉnh

### Thay Đổi Kích Thước RSA Key
Mở `HybridCryptoManager.java` → Thay đổi:
```java
private static final int RSA_KEY_SIZE = 2048;  // Chỉnh thành 4096 cho mạnh hơn
```

### Thay Đổi Kích Thước AES Key
```java
private static final int AES_KEY_SIZE = 256;   // Hoặc 128 hoặc 192
```

### Tái Tạo RSA Keys
Xóa files:
- `public_key.txt`
- `private_key.txt`

Lần tiếp theo chạy server, keys mới sẽ được tạo.

---

## ⚠️ Lưu Ý Bảo Mật

1. **Private key phải bí mật**: Không chia sẻ `private_key.txt`
2. **Public key công khai**: Có thể chia sẻ `public_key.txt` tự do
3. **Session key ngẫu nhiên**: Mỗi request/response dùng key khác nhau
4. **RSA 2048-bit**: Đủ an toàn cho 30 năm tới (theo NIST)
5. **AES 256-bit**: Không bao giờ bị crack nếu key ngẫu nhiên

---

## 🧪 Kiểm Tra Hoạt Động

### Server Console
```
[192.168.1.100:54321] [INFO] Luồng xử lý đã mở. Thực hiện Key Exchange...
[192.168.1.100:54321] [INFO] Đã gửi Public Key cho Client.
[192.168.1.100:54321] [INFO] ✓ Giải mã Hybrid thành công! Ngôn ngữ ID: 62 | Chỉ Format: false
[192.168.1.100:54321] [INFO] Đang đẩy code sang Judge0 API...
[192.168.1.100:54321] [INFO] Code chuẩn xác! Đã format và lấy output.
[192.168.1.100:54321] [INFO] ✓ Đã mã hóa Hybrid và gửi response về Client!
```

### Client Console
```
[CLIENT] ✓ Nhận public key từ server (Key Exchange thành công!)
[CLIENT] ✓ Mã hóa Hybrid thành công!
[CLIENT] → Session Key (RSA): MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8...
[CLIENT] → Data (AES): aDZrN...
[CLIENT] ✓ Đã gửi dữ liệu mã hóa tới server
[CLIENT] ✓ Nhận response mã hóa từ server
[CLIENT] ✓ Giải mã Hybrid thành công!
```

---

## 📚 Tham Khảo

- **RSA**: https://en.wikipedia.org/wiki/RSA_(cryptosystem)
- **AES**: https://en.wikipedia.org/wiki/Advanced_Encryption_Standard
- **Hybrid Encryption**: https://en.wikipedia.org/wiki/Hybrid_cryptosystem
- **Java Cryptography**: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/package-summary.html

---

**Version**: 1.0  
**Ngày Cập Nhật**: April 17, 2026  
**Trạng Thái**: ✅ Hoạt động đầy đủ
