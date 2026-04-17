# 🚀 Quick Start - Mã Hóa Lai

## ⏱️ 5 Phút để Hiểu & Chạy

### 1️⃣ Cài Đặt (2 phút)

```bash
# Cài Java 21 (nếu chưa có)
# Windows: https://www.oracle.com/java/technologies/downloads/#java21
# macOS: brew install openjdk@21
# Linux: sudo apt install openjdk-21-jdk

# Cài Maven (nếu chưa có)
# Windows: choco install maven
# macOS: brew install maven
# Linux: sudo apt install maven

# Kiểm tra
java -version    # Java 21+
mvn -version     # Maven 3.6+
```

### 2️⃣ Build (2 phút)

```bash
cd d:\LTM-codechecker

# Biên dịch
mvn clean compile

# Build JAR
mvn clean package -DskipTests
```

### 3️⃣ Chạy (1 phút)

**Terminal 1 - Server**:
```bash
cd d:\LTM-codechecker
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.server.ServerMain"

# Output:
# [+] Tạo cặp RSA keys mới...
# [+] Public key đã lưu vào: public_key.txt
# [+] Private key đã lưu vào: private_key.txt
# [INFO] Server đang lắng nghe kết nối tại port 5000...
```

**Terminal 2 - Client** (mở terminal mới):
```bash
cd d:\LTM-codechecker
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.client.ClientUIFrame"

# Cửa sổ GUI mở → Nhập code → Click "Check"
```

---

## 🔐 Mã Hóa Lai Là Gì?

```
Trước (Cũ):
  Client --[AES("fixedKey")]-->Server
  ⚠️ Key cố định = Risk cao

Sau (Mới):
  Client 1️⃣ Nhận public key RSA từ server
         2️⃣ Tạo random session key AES
         3️⃣ Mã hóa: RSA(key) + AES(data)
  Server 4️⃣ Giải mã: RSA private key
         5️⃣ AES decrypt data
  ✓ Session key mới mỗi lần = Bảo mật cao
```

---

## 📊 Thống Kê

| Tiêu chí | Con số |
|----------|--------|
| 🔐 RSA Key Size | 2048-bit |
| 🔑 AES Key Size | 256-bit |
| 📁 File Mới Thêm | 2 |
| 📝 File Cập Nhật | 4 |
| 📚 Tài Liệu | 3 |
| 📄 Dòng Code | +800 |

---

## 🧪 Test Nhanh

### Test 1: Kiểm Tra Key Exchange
```
Server console:
  [192.168.x.x:xxxxx] [INFO] Đã gửi Public Key cho Client.

Client console:
  [CLIENT] ✓ Nhận public key từ server (Key Exchange thành công!)
  [CLIENT] ✓ Mã hóa Hybrid thành công!

✓ Key Exchange hoạt động!
```

### Test 2: Kiểm Tra Code Check
```
Client GUI:
  1. Paste code Java:
     public class Hello {
         public static void main(String[] args) {
             System.out.println("Hello!");
         }
     }
  2. Chọn "Java" from dropdown
  3. Click "Check"
  4. Kết quả: "Hello!"

✓ Mã hóa hybrid hoạt động!
```

---

## 🔍 Kiểm Tra Files

```bash
# Sau khi chạy server, check:
ls public_key.txt    # ✓ Public key (gửi cho client)
ls private_key.txt   # ✓ Private key (bí mật server)

# Nội dung (Base64, không đọc được):
cat public_key.txt
# MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
```

---

## 📁 File Cấu Trúc

```
Mới:
  ✨ HybridCryptoManager.java    → RSA + AES encryption
  ✨ HYBRID_ENCRYPTION.md        → Tài liệu chi tiết
  ✨ BUILD_GUIDE.md              → Hướng dẫn build
  ✨ CHANGES.md                  → Thay đổi chi tiết
  ✨ QUICK_START.md              → File này
  ✨ public_key.txt              → Generated lần đầu
  ✨ private_key.txt             → Generated lần đầu

Cập Nhật:
  🔄 ServerMain.java             → Load/generate RSA keys
  🔄 ClientHandler.java          → Hybrid decrypt/encrypt
  🔄 ClientService.java          → Hybrid encrypt/decrypt
  🔄 KeyManager.java             → RSA key management
```

---

## 🎯 Flow Diagram

```
┌─────────────┐                    ┌─────────────┐
│   Client    │                    │   Server    │
├─────────────┤                    ├─────────────┤
│  Connect    │--TCP Port 5000-->  │  Listening  │
└─────────────┘                    └─────────────┘
       │                                  │
       │<--- Public Key (RSA) -----------│ Key Exchange
       │                                  │
       │ 1. Create Session Key AES        │
       │ 2. Encrypt Key with RSA pub      │
       │ 3. Encrypt Data with AES        │
       │                                  │
       │--- EncryptedKey (RSA) -------->  │
       │--- EncryptedData (AES) ------>  │
       │                                  │
       │                         1. Decrypt Key RSA priv
       │                         2. Decrypt Data AES
       │                         3. Process Request
       │                         4. Encrypt Response
       │                                  │
       │<--- EncryptedKey (RSA) --------│
       │<--- EncryptedData (AES) ------│
       │                                  │
       │ 1. Decrypt Key RSA pub          │
       │ 2. Decrypt Data AES             │
       │ 3. Display Result               │
       │                                  │
```

---

## ⚙️ Tuỳ Chỉnh (Optional)

### Đổi RSA Key Size
`HybridCryptoManager.java`:
```java
private static final int RSA_KEY_SIZE = 2048;  // → 4096
```

### Đổi AES Key Size
```java
private static final int AES_KEY_SIZE = 256;   // → 128
```

### Đổi Server Port
`ServerMain.java` + `ClientService.java`:
```java
private static final int PORT = 5000;  // → 8080
```

---

## 🐛 Troubleshooting

| Lỗi | Giải Pháp |
|-----|----------|
| `mvn: command not found` | Cài Maven, thêm vào PATH |
| `Port 5000 already in use` | Kill process: `netstat -ano \| findstr :5000` |
| `Connection refused` | Kiểm tra Server có chạy không |
| `Compile failed` | Kiểm tra Java 21, Javac hoạt động |
| `Keys not generated` | Xóa `public_key.txt`, chạy lại Server |

---

## 📚 Đọc Thêm

- **Chi tiết mã hóa**: [HYBRID_ENCRYPTION.md](HYBRID_ENCRYPTION.md)
- **Hướng dẫn build**: [BUILD_GUIDE.md](BUILD_GUIDE.md)
- **Thay đổi chi tiết**: [CHANGES.md](CHANGES.md)
- **README gốc**: [Readme.md](Readme.md)

---

## 🎓 Học Thêm

### Khái Niệm
- RSA (Asymmetric): Public key mã hóa, private key giải mã
- AES (Symmetric): Cùng key mã hóa + giải mã
- Hybrid: RSA dùng cho key exchange, AES dùng cho data

### Lợi Ích
- 🔐 **Bảo mật cao**: RSA 2048-bit + AES 256-bit
- ⚡ **Hiệu suất tốt**: AES nhanh, RSA chỉ cho key
- 🔄 **Forward Secrecy**: Session key mới mỗi lần
- 🌐 **Scalable**: Hỗ trợ multi-client

---

## ✅ Checklist Lần Đầu

- [ ] Java 21 cài đặt
- [ ] Maven cài đặt + PATH
- [ ] `mvn clean compile` ✓
- [ ] `mvn clean package` ✓
- [ ] Server khởi động → public_key.txt tạo ✓
- [ ] Client kết nối → "Key Exchange" log ✓
- [ ] Test code Java check ✓
- [ ] Console hiện "Hybrid Encrypt/Decrypt" ✓

---

## 🚀 Tiếp Theo

1. **Thay đổi cấu hình**: Tuỳ chỉnh RSA/AES size
2. **Test với code phức tạp**: Thử nhiều ngôn ngữ
3. **Deploy**: Chạy trên server thực
4. **Monitor**: Kiểm tra logs, performance
5. **Scale**: Hỗ trợ nhiều clients đồng thời

---

## 💡 Tips & Tricks

```bash
# Run server + show detailed logs
mvn exec:java -Dexec.mainClass="..." -X

# Run in background (Linux)
mvn exec:java -Dexec.mainClass="..." &

# Build without running tests
mvn clean package -DskipTests

# Show dependency tree
mvn dependency:tree

# Format code
mvn spotless:apply

# Check for security issues
mvn clean security:check
```

---

## 📞 Support

Nếu gặp vấn đề:
1. Xem console output/logs
2. Kiểm tra `BUILD_GUIDE.md` troubleshooting section
3. Xem `HYBRID_ENCRYPTION.md` chi tiết
4. Verify `public_key.txt` + `private_key.txt` tồn tại

---

**Happy Coding! 🎉**

---

*Version: 1.0 | Updated: April 17, 2026*
