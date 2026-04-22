# 🔐 Phân Tích Chi Tiết Luồng Mã Hóa Lai (Hybrid Encryption Flow)

**Phiên Bản**: 1.0  
**Ngày**: April 17, 2026  
**Mục Đích**: Giải thích từng bước luồng RSA + AES trong LTM Code Checker  

---

## 📋 Mục Lục

1. [Tổng Quan](#-tổng-quan)
2. [Kiến Thức Nền Tảng](#-kiến-thức-nền-tảng)
3. [Phase 1: Key Exchange](#-phase-1-key-exchange)
4. [Phase 2: Request Encryption](#-phase-2-request-encryption)
5. [Phase 3: Request Decryption](#-phase-3-request-decryption)
6. [Phase 4: Processing](#-phase-4-processing--response-creation)
7. [Phase 5: Response Encryption](#-phase-5-response-encryption)
8. [Phase 6: Response Decryption](#-phase-6-response-decryption)
9. [Complete Workflow Diagram](#-complete-workflow-diagram)
10. [Key Points & Common Mistakes](#-key-points--common-mistakes)
11. [Security Analysis](#-security-analysis)
12. [Testing & Verification](#-testing--verification)

---

# 🎯 Tổng Quan

## Hybrid Encryption là gì?

**Hybrid Encryption** = **RSA (Asymmetric) + AES (Symmetric)**

| Thuộc Tính | RSA | AES |
|-----------|-----|-----|
| **Loại** | Asymmetric (public/private) | Symmetric (shared key) |
| **Kích Khóa** | 2048-bit | 256-bit |
| **Tốc Độ** | Chậm (~1-10ms) | Nhanh (~0.1ms) |
| **Dùng Cho** | Key exchange | Data encryption |
| **Scalability** | ❌ Không dùng cho data lớn | ✅ Dùng cho data lớn |

---

## Tại Sao Dùng Hybrid?

```
❌ WRONG: Chỉ dùng RSA cho tất cả
├─ Bảo mật ✅
├─ Nhưng chậm ❌ (chạy code lớn sẽ lag)
└─ Không scalable ❌

✅ RIGHT: Hybrid (RSA + AES)
├─ RSA: Trao đổi session key (nhanh, 1 lần)
├─ AES: Mã hóa dữ liệu (nhanh, reuse key)
└─ Bảo mật + hiệu suất ✓
```

---

## Quy Trình Tổng Quát

```
┌─────────┐  Public Key  ┌─────────┐
│ CLIENT  │◄────────────│ SERVER  │ (Phase 1: Key Exchange)
│         │             │         │
├─────────┤             ├─────────┤
│ Generate│ AES Key+RSA │ Decrypt │ (Phase 2-3: Encrypt Request)
│ Request │────────────→│ Request │
│         │             │         │
│         │ Process     │ Run     │ (Phase 4: Process)
│         │←────────────│ Judge0  │
│         │ Response    │ Create  │
│         │             │ Response│ (Phase 5: Encrypt Response)
│         │             │         │
│ Decrypt │ Encrypted   │ Reuse   │
│ Response│◄────────────│ AES Key │ (Phase 6: Decrypt Response)
└─────────┘             └─────────┘
```

---

# 📚 Kiến Thức Nền Tảng

## RSA (Rivest-Shamir-Adleman)

**Hoạt động**:
```
Plaintext → [Encrypt với public key] → Ciphertext
Ciphertext → [Decrypt với private key] → Plaintext
```

**Đặc điểm**:
- ✅ Asymmetric (public/private keys khác nhau)
- ✅ Chỉ owner (có private key) mới decrypt
- ✅ An toàn cho key exchange
- ❌ Chậm, không dùng cho data lớn
- ❌ Chỉ mã hóa được ~245 bytes (2048-bit)

**Ví Dụ Java**:
```java
KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
kpg.initialize(2048);
KeyPair kp = kpg.generateKeyPair();
PublicKey pubKey = kp.getPublic();
PrivateKey privKey = kp.getPrivate();
```

---

## AES (Advanced Encryption Standard)

**Hoạt động**:
```
Plaintext → [Encrypt với session key] → Ciphertext
Ciphertext → [Decrypt với same key] → Plaintext
```

**Đặc điểm**:
- ✅ Symmetric (cùng key để encrypt/decrypt)
- ✅ Nhanh, dùng được cho data lớn
- ✅ Standard: AES-256 (256-bit key)
- ❌ Cần trao đổi key an toàn trước (dùng RSA)

**Ví Dụ Java**:
```java
KeyGenerator kg = KeyGenerator.getInstance("AES");
kg.init(256);
SecretKey aesKey = kg.generateKey();  // Random key

Cipher cipher = Cipher.getInstance("AES");
cipher.init(Cipher.ENCRYPT_MODE, aesKey);
byte[] encrypted = cipher.doFinal(plaintext.getBytes());
```

---

## Base64 Encoding

**Tại sao?**
- TCP Socket truyền text, không truyền binary được
- Mã hóa menghasilkan bytes ngẫu nhiên (binary)
- Dùng Base64 để convert binary → text safe

**Ví Dụ**:
```
Original: {0xAB, 0xCD, 0xEF} (binary)
Base64:   "q83v" (text)
Decode:   {0xAB, 0xCD, 0xEF} (binary lại)
```

---

# 🔄 Phase 1: Key Exchange

## Mục Đích
Server gửi RSA public key cho Client. Client sẽ dùng key này để mã hóa AES session key trong request.

## Timeline

```
CLIENT                              SERVER
  │                                   │
  │◄────────────────────────────────│
  │  Public Key (Base64 string)     │
  │                                  │
```

## Chi Tiết Bước

### 1️⃣ Client Kết Nối TCP

```java
// ClientService.java
Socket socket = new Socket("localhost", 5000);
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
```

**Kết quả**: TCP connection established

### 2️⃣ Server Accept Connection

```java
// ServerMain.java
ServerSocket serverSocket = new ServerSocket(5000);
Socket clientSocket = serverSocket.accept();  // ← Chặn đến khi có client
PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
```

**Kết quả**: Server nhận client connection

### 3️⃣ Server Gửi Public Key

```java
// ClientHandler.java
String publicKeyBase64 = rsaKeyPair.getPublicKey();  // Base64 encoded
out.println(publicKeyBase64);
out.flush();
```

**Format**:
```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2F7...
(long Base64 string, ~400 characters)
```

### 4️⃣ Client Nhận Public Key

```java
// ClientService.java
String serverPublicKeyBase64 = in.readLine();  // ← Nhận từ server
System.out.println("Received public key: " + serverPublicKeyBase64.substring(0, 50) + "...");
```

**Kết quả**: Client có public key của server

## Sequence Diagram

```
CLIENT                              SERVER
  │                                   │
  │────────── TCP Connect ──────────→ │
  │                                   │
  │◄─── Public Key (Base64) ──────────│
  │ "MIIBIjANBgkq..."                │
  │                                   │
  ├─ Store public key                 │
  │ for later encryption              │
  │
```

---

# 🔐 Phase 2: Request Encryption

## Mục Đích
Client tạo random AES key, mã hóa nó với RSA public key, mã hóa request với AES key, rồi gửi 2 dòng lên server.

## Timeline

```
CLIENT                              SERVER
  │ Prepare request data
  ├─ Generate random AES key
  ├─ Encrypt AES key with RSA pub
  ├─ Encrypt request with AES
  │
  ├─ Send encrypted AES key ────────→ │
  ├─ Send encrypted request ────────→ │
  │
```

## Chi Tiết Bước

### 1️⃣ Tạo Request JSON

```java
// ClientUIFrame.java
RequestPayload payload = new RequestPayload(code, languageId, isFormatOnly);
// Example:
{
  "sourceCode": "print('hello')",
  "languageId": 92,
  "isFormatOnly": false
}
```

### 2️⃣ Tạo Random AES Key

```java
// HybridCryptoManager.java
public SecretKey generateAESKey() {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(256);  // 256-bit key
    return kg.generateKey();  // Random key
}
```

**Kết Quả**: `SecretKey` object, 32 bytes random

### 3️⃣ Mã Hóa AES Key Với RSA Public Key

```java
// HybridCryptoManager.java
Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
PublicKey pubKey = loadPublicKeyFromBase64(serverPublicKeyBase64);
rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());
// ↑ aesKey.getEncoded() = 32 bytes plaintext
// ↓ encryptedAesKey = 256 bytes (RSA 2048-bit)

String encryptedSessionKeyBase64 = Base64.getEncoder().encodeToString(encryptedAesKey);
// ↓ "q83vABC123...XYZ"
```

**Flow**:
```
Random AES Key (32 bytes)
    ↓
[Encrypt with RSA public key]
    ↓
Encrypted AES Key (256 bytes)
    ↓
[Base64 encode]
    ↓
"MIIBIjANBgkqhkiG9w0BAQEFAA..." (text, ~350 chars)
```

### 4️⃣ Mã Hóa Request Với AES Key

```java
// HybridCryptoManager.java
String requestJson = gson.toJson(requestPayload);

Cipher aesCipher = Cipher.getInstance("AES");
aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
byte[] encryptedRequest = aesCipher.doFinal(requestJson.getBytes(StandardCharsets.UTF_8));

String encryptedRequestBase64 = Base64.getEncoder().encodeToString(encryptedRequest);
```

**Flow**:
```
Request JSON (variable size, e.g. 500 bytes)
    ↓
[Encrypt with AES key]
    ↓
Encrypted Request (same size + padding, ~512 bytes)
    ↓
[Base64 encode]
    ↓
"q83vABC123...XYZ" (text, ~680 chars)
```

### 5️⃣ Gửi 2 Dòng Lên Server

```java
// ClientService.java
out.println(encryptedSessionKeyBase64);  // Dòng 1
out.println(encryptedRequestBase64);     // Dòng 2
out.flush();

// Store plaintext AES key để dùng sau khi decrypt response
this.sessionKeyPlaintext = aesKey;
```

**Network Transmission**:
```
Line 1: "MIIBIjANBgkq...q83vABC123...XYZ" (RSA encrypted AES key)
Line 2: "q83vABC123...XYZ" (AES encrypted request)
```

## Sequence Diagram

```
CLIENT                              SERVER
  │
  ├─ Create request JSON
  │  {sourceCode: "...", languageId: 92}
  │
  ├─ Generate random AES-256 key
  │  32 bytes: {0xAB, 0xCD, ...}
  │
  ├─ Encrypt AES key with RSA public
  │  256 bytes encrypted
  │  Base64: "MIIBIjA..."
  │
  ├─ Encrypt request JSON with AES
  │  ~512 bytes encrypted
  │  Base64: "q83vABC..."
  │
  ├─ Send encrypted AES key ────────→ │
  │ "MIIBIjA..."                     │
  │                                  │
  ├─ Send encrypted request ────────→ │
  │ "q83vABC..."                     │
  │                                  │
  ├─ Store plaintext AES key        │
  │  (for response decryption later)│
```

---

# 🔓 Phase 3: Request Decryption

## Mục Đích
Server nhận 2 dòng, giải mã RSA để lấy AES key, giải mã AES để lấy request JSON, parse request.

## Timeline

```
CLIENT                              SERVER
                                    │ Receive encrypted AES key
                                    ├─ Decrypt with RSA private
                                    ├─ Get plaintext AES key
                                    │
                                    │ Receive encrypted request
                                    ├─ Decrypt with AES key
                                    ├─ Get request JSON
                                    ├─ Parse RequestPayload
```

## Chi Tiết Bước

### 1️⃣ Server Nhận 2 Dòng

```java
// ClientHandler.java
String encryptedSessionKeyBase64 = in.readLine();  // Dòng 1
String encryptedRequestDataBase64 = in.readLine();  // Dòng 2
```

### 2️⃣ Giải Mã RSA - Lấy AES Key

```java
// HybridCryptoManager.java
public SecretKey decryptSessionKey(String encryptedSessionKeyBase64) {
    // 1. Base64 decode
    byte[] encryptedAesKey = Base64.getDecoder().decode(encryptedSessionKeyBase64);
    
    // 2. RSA decrypt (chỉ server có private key!)
    Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] plaintextAesKey = rsaCipher.doFinal(encryptedAesKey);  // 32 bytes
    
    // 3. Tạo SecretKey object
    SecretKey aesKey = new SecretKeySpec(plaintextAesKey, 0, plaintextAesKey.length, "AES");
    
    return aesKey;
}
```

**Flow**:
```
"MIIBIjA..." (text)
    ↓
[Base64 decode]
    ↓
Encrypted AES Key (256 bytes binary)
    ↓
[Decrypt with RSA private key]
    ↓
Plaintext AES Key (32 bytes)
    ↓
[Create SecretKey object]
    ↓
SecretKey object
```

### 3️⃣ Giải Mã AES - Lấy Request JSON

```java
// ClientHandler.java
SecretKey sessionKey = hybridCryptoManager.decryptSessionKey(encryptedSessionKeyBase64);

String decryptedRequest = hybridCryptoManager.decryptDataWithAES(
    encryptedRequestDataBase64,
    sessionKey
);
// Result: {"sourceCode":"print('hello')","languageId":92,"isFormatOnly":false}
```

```java
// HybridCryptoManager.java
public String decryptDataWithAES(String encryptedDataBase64, SecretKey aesKey) {
    // 1. Base64 decode
    byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
    
    // 2. AES decrypt
    Cipher aesCipher = Cipher.getInstance("AES");
    aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
    byte[] plaintextBytes = aesCipher.doFinal(encryptedData);
    
    // 3. Convert to String
    return new String(plaintextBytes, StandardCharsets.UTF_8);
}
```

### 4️⃣ Parse Request JSON

```java
// ClientHandler.java
RequestPayload request = gson.fromJson(decryptedRequest, RequestPayload.class);
// Now we have:
// - request.sourceCode = "print('hello')"
// - request.languageId = 92
// - request.isFormatOnly = false
```

## Sequence Diagram

```
                                    SERVER
                                    │
                                    ├─ Receive line 1: "MIIBIjA..."
                                    │
                                    ├─ Base64 decode
                                    │
                                    ├─ RSA decrypt with private key
                                    │  ↓ Get plaintext AES key (32 bytes)
                                    │
                                    ├─ Receive line 2: "q83vABC..."
                                    │
                                    ├─ Base64 decode
                                    │
                                    ├─ AES decrypt with session key
                                    │  ↓ Get JSON plaintext
                                    │
                                    ├─ Parse JSON
                                    │  {sourceCode: "print('hello')", ...}
                                    │
                                    ├─ Extract fields
                                    │  code = "print('hello')"
                                    │  langId = 92
                                    │  isFormatOnly = false
```

---

# ⚙️ Phase 4: Processing & Response Creation

## Mục Đích
Server xử lý request (chạy code qua Judge0 hoặc format code), tạo response.

## Chi Tiết

### 1️⃣ Kiểm Tra Flag

```java
// ClientHandler.java
if (request.isFormatOnly()) {
    // YÊU CẦU 1: CHỈ FORMAT CODE
    // ...
} else {
    // YÊU CẦU 2: CHẠY CODE
    // ...
}
```

### 2️⃣ Nếu Format Only

```java
String formattedCode = formatter.formatCode(request.getSourceCode(), request.getLanguageId());
responsePayload = new ResponsePayload(true, "Formatted!", formattedCode, null);
```

### 3️⃣ Nếu Check & Run

```java
// Gọi Judge0 API
String apiResultJson = api.compileAndRun(request.getSourceCode(), request.getLanguageId());
JSONObject jsonResponse = new JSONObject(apiResultJson);

int statusId = jsonResponse.getJSONObject("status").getInt("id");

if (statusId == 3) {
    // ✅ Code chạy thành công
    String output = jsonResponse.optString("stdout", "");
    String formatted = formatter.formatCode(request.getSourceCode(), request.getLanguageId());
    responsePayload = new ResponsePayload(true, output, formatted, null);
} else {
    // ❌ Code lỗi
    String errorOutput = jsonResponse.optString("compile_output", "");
    List<ErrorLog> errors = checker.parseErrors(errorOutput, request.getLanguageId());
    responsePayload = new ResponsePayload(false, errorOutput, null, errors);
}
```

### 4️⃣ Serialize Response

```java
String responseJson = gson.toJson(responsePayload);
// Result: {"isSuccess":true,"output":"hello\n","formattedCode":"print('hello')","errors":null}
```

## Sequence Diagram

```
                                    SERVER
                                    │
                                    ├─ Check: isFormatOnly?
                                    │
                                    ├─ YES: Format code
                                    │  └─ Call Godbolt API
                                    │
                                    ├─ NO: Run code
                                    │  ├─ Call Judge0 API
                                    │  ├─ Parse output
                                    │  ├─ Parse errors (if any)
                                    │  └─ Format code (if success)
                                    │
                                    ├─ Create ResponsePayload
                                    │  {isSuccess: true/false, output: "...", ...}
                                    │
                                    ├─ Serialize to JSON
                                    │  "{\"isSuccess\":true,...}"
```

---

# 🔐 Phase 5: Response Encryption

## ⚠️ CRITICAL POINT: Session Key Reuse

### ❌ WRONG: Create NEW AES Key

```
❌ Server creates NEW AES key for response:
   ├─ encryptedMsg = encryptHybridWithKey(responseJson, serverPublicKey)
   │  (creates NEW random AES key)
   ├─ Send: encrypted NEW AES key + encrypted response
   └─ Client receives:
      ├─ Tries to decrypt NEW AES key with... RSA PRIVATE KEY?
      ├─ Client doesn't have private key!
      └─ ❌ CRASH: "Private key not initialized!"
```

### ✅ RIGHT: Reuse Client's AES Key

```
✅ Server REUSES AES key from request:
   ├─ sessionKey = (already decrypted from request)
   ├─ encryptedResponse = encryptDataWithAES(responseJson, sessionKey)
   │  (uses SAME AES key from request)
   └─ Send: SAME encrypted AES key + encrypted response
      └─ Client receives:
         ├─ Already has plaintext AES key from earlier
         ├─ Decrypt response with plaintext key
         └─ ✅ SUCCESS!
```

## Chi Tiết Bước

### 1️⃣ Serialize Response

```java
// ClientHandler.java (already done in Phase 4)
String responseJson = gson.toJson(responsePayload);
```

### 2️⃣ Mã Hóa Response Với AES Key (Reuse!)

```java
// ClientHandler.java
String encryptedResponse = hybridCryptoManager.encryptDataWithAES(
    responseJson,
    sessionKey  // ← REUSE từ request decryption!
);
```

```java
// HybridCryptoManager.java
public String encryptDataWithAES(String plaintext, SecretKey aesKey) {
    Cipher aesCipher = Cipher.getInstance("AES");
    aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
    byte[] encrypted = aesCipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
}
```

### 3️⃣ Gửi Response

```java
// ClientHandler.java
out.println(encryptedSessionKey);  // ← Cái CỦA CUÁ từ request (không tạo mới!)
out.println(encryptedResponse);     // ← Response mới (encrypted với same key)
out.flush();
```

## Sequence Diagram

```
                                    SERVER
                                    │
                                    ├─ Response JSON created
                                    │  {isSuccess: true, output: "hello\n"}
                                    │
                                    ├─ Encrypt with SAME AES key
                                    │  (from request decryption)
                                    │
                                    ├─ ⚠️ KEY POINT:
                                    │  └─ REUSE sessionKey (don't create new!)
                                    │
                                    ├─ Get encrypted response
                                    │  "q83vXYZ..."
                                    │
                                    ├─ Send encrypted AES key ────────→ │
                                    │ (SAME one from request)          │
                                    │                                  │
                                    ├─ Send encrypted response ────────→ │
                                    │ "q83vXYZ..."                    │
```

---

# 🔓 Phase 6: Response Decryption

## Mục Đích
Client nhận response, dùng plaintext AES key được lưu từ phase 2, decrypt response, display.

## Chi Tiết Bước

### 1️⃣ Nhận 2 Dòng

```java
// ClientService.java
String encryptedSessionKey = in.readLine();        // Dòng 1
String encryptedResponseData = in.readLine();      // Dòng 2
```

### 2️⃣ Không Decrypt AES Key (Đã Có!)

```java
// ClientService.java
// ✅ KHÔNG làm cái này:
// SecretKey newSessionKey = hybridCryptoManager.decryptSessionKey(encryptedSessionKey);
// ❌ Client không có private key!

// ✅ LÀM CÁI NÀY:
// Dùng plaintext AES key được lưu từ Phase 2
// this.sessionKeyPlaintext = encryptedMessage.getSessionKey();
```

### 3️⃣ Giải Mã Response Với AES Key

```java
// ClientService.java
String decryptedResponse = hybridCryptoManager.decryptDataWithAES(
    encryptedResponseData,
    sessionKeyPlaintext  // ← Plaintext key từ Phase 2!
);
// Result: {"isSuccess":true,"output":"hello\n",...}
```

### 4️⃣ Parse Response

```java
// ClientService.java
ResponsePayload response = gson.fromJson(decryptedResponse, ResponsePayload.class);
// Now we have:
// - response.isSuccess() = true
// - response.getOutput() = "hello\n"
// - response.getFormattedCode() = "print('hello')"
```

### 5️⃣ Display GUI

```java
// ClientUIFrame.java (in done() method)
if (response.isSuccess()) {
    consoleOutput.append("\n=== RESULT ===\n");
    consoleOutput.append(response.getOutput());
} else {
    consoleOutput.append("\n=== ERRORS ===\n");
    for (ErrorLog error : response.getErrors()) {
        consoleOutput.append("Line " + error.getLine() + ": " + error.getMessage() + "\n");
    }
}
```

## Sequence Diagram

```
CLIENT                              SERVER
  │                                   │
  │◄─ Receive encrypted AES key ─────│
  │ "MIIBIjA..."                     │
  │                                  │
  ├─ Check stored plaintext key     │
  │ YES - Have it from Phase 2!     │
  │                                  │
  │◄─ Receive encrypted response ────│
  │ "q83vXYZ..."                     │
  │                                  │
  ├─ Base64 decode                  │
  │                                  │
  ├─ AES decrypt with plaintext key │
  │ (from Phase 2)                   │
  │                                  │
  ├─ Get response JSON              │
  │ {isSuccess: true, output: "hello"}
  │                                  │
  ├─ Parse response                 │
  │                                  │
  ├─ Update GUI                     │
  │ Display output in console       │
  │
```

---

# 🔄 Complete Workflow Diagram

## End-to-End Flow Chart

```
╔════════════════════════════════════════════════════════════════╗
║                     COMPLETE WORKFLOW                          ║
╚════════════════════════════════════════════════════════════════╝

CLIENT SIDE                         SERVER SIDE
═══════════════════════════════════════════════════════════════

① CONNECT & KEY EXCHANGE
   TCP connect                    ← ServerSocket.accept()
   │
   ├─────────────────────────────→  [Listen]
   │
   ◄─────────────────────────────  Public Key (Base64)
   │ MIIBIjAN...
   │
   Store public key for encryption


② REQUEST ENCRYPTION
   Generate random AES-256 key
   ├─ 32 bytes random
   │
   RSA encrypt AES key
   ├─ Using server's public key
   ├─ Result: 256 bytes encrypted
   ├─ Base64: "MIIBIjA..."
   │
   Create request JSON
   ├─ {sourceCode: "print('hello')", ...}
   │
   AES encrypt request
   ├─ Using session key
   ├─ Result: ~512 bytes encrypted
   ├─ Base64: "q83vABC..."
   │
   Store plaintext AES key for later
   │
   ├─────────────────────────────→  Encrypted AES key ────────────→
   │                               │ Encrypted request
   │                               ├─ [Receive line 1]
   │                               ├─ [Receive line 2]


③ REQUEST DECRYPTION
                                    Base64 decode line 1
                                    ├─ 256 bytes encrypted
                                    │
                                    RSA decrypt with private key
                                    ├─ Get plaintext AES key (32 bytes)
                                    │
                                    Base64 decode line 2
                                    ├─ ~512 bytes encrypted
                                    │
                                    AES decrypt with session key
                                    ├─ Get request JSON
                                    │
                                    Parse RequestPayload
                                    ├─ sourceCode = "print('hello')"
                                    ├─ languageId = 92
                                    └─ isFormatOnly = false


④ PROCESSING & RESPONSE
                                    Check isFormatOnly?
                                    │
                                    ├─ YES: Format code
                                    │  └─ Call Godbolt API
                                    │
                                    ├─ NO: Run code
                                    │  ├─ Call Judge0 API
                                    │  ├─ statusId == 3?
                                    │  │  ├─ YES: output success
                                    │  │  └─ NO: parse errors
                                    │  └─ Format code
                                    │
                                    Create ResponsePayload
                                    ├─ isSuccess: true/false
                                    ├─ output: "hello\n"
                                    └─ formattedCode: "..."
                                    │
                                    Serialize to JSON
                                    └─ "{\"isSuccess\":true,...}"


⑤ RESPONSE ENCRYPTION (KEY POINT!)
                                    ⚠️ REUSE sessionKey from request!
                                    │
                                    ├─ DON'T create new AES key
                                    ├─ USE existing sessionKey
                                    │
                                    AES encrypt response
                                    ├─ With SAME key
                                    ├─ Result: ~512 bytes encrypted
                                    ├─ Base64: "q83vXYZ..."
                                    │
                                    ├─────────────────────────────←
                                    │  Encrypted AES key (same)
   ◄─────────────────────────────  Encrypted response
   │ "q83vXYZ..."


⑥ RESPONSE DECRYPTION
   Already have plaintext AES key!
   ├─ From Phase 2 (stored)
   │
   Receive encrypted response
   │
   Base64 decode
   ├─ ~512 bytes encrypted
   │
   AES decrypt with plaintext key
   ├─ Get response JSON
   │
   Parse ResponsePayload
   ├─ isSuccess = true
   ├─ output = "hello\n"
   └─ formattedCode = "..."
   │
   Update GUI
   ├─ Display output
   ├─ Highlight syntax
   └─ Show any errors
```

---

# 🔑 Key Points & Common Mistakes

## ✅ What's Correct

1. **RSA 2048-bit** cho key exchange
2. **AES 256-bit** cho data encryption
3. **Session key random per request**
4. **Reuse session key cho response** (CRITICAL!)
5. **Base64 encode** để transport
6. **Server has both keys** (public + private)
7. **Client has only public key**

## ❌ Common Mistakes

### Mistake 1: Using Public Key to Decrypt

```java
// ❌ WRONG
SecretKey key = hybridCryptoManager.decryptSessionKey(encrypted);
// Client tries to decrypt with private key it doesn't have
// CRASH: "Private key not initialized!"
```

**Fix**: Reuse plaintext key from request

### Mistake 2: Creating NEW AES Key for Response

```java
// ❌ WRONG
HybridEncryptedMessage msg = encryptHybridWithKey(response, pubKey);
out.println(msg.getEncryptedSessionKey());  // NEW key
out.println(msg.getEncryptedData());        // encrypted with NEW key
```

**Fix**: Reuse existing session key

```java
// ✅ RIGHT
String encrypted = encryptDataWithAES(response, existingSessionKey);
out.println(encryptedSessionKey);  // OLD key (for protocol)
out.println(encrypted);             // encrypted with SAME key
```

### Mistake 3: Not Storing Plaintext Key

```java
// ❌ WRONG
// Don't store plaintext key → can't decrypt response
HybridEncryptedMessageWithKey msg = encryptHybridWithKey(...);
// ← Don't save msg.getSessionKey()
```

**Fix**: Store plaintext key

```java
// ✅ RIGHT
HybridEncryptedMessageWithKey msg = encryptHybridWithKey(...);
this.sessionKeyPlaintext = msg.getSessionKey();  // Save for later!
```

### Mistake 4: Using "public class" for Code Submission

```java
// ❌ WRONG - Judge0 can't find entry point
public class PrimeChecker {
    // File name is Main.java, not PrimeChecker.java
}
```

**Fix**: Use matching class name or remove public

```java
// ✅ RIGHT
class PrimeChecker {  // No public
    // or
public class Main {    // Matches file name
```

---

# 🔐 Security Analysis

## Threat Model & Mitigations

| Threat | Attack | Mitigation |
|--------|--------|-----------|
| **Wiretap** | Attacker captures network traffic | RSA 2048 + AES 256 encryption |
| **Replay Attack** | Reuse old request | Random AES key per request |
| **Man-in-Middle** | Intercept key exchange | Certificate pinning (future) |
| **Brute Force** | Crack encryption | 2048-bit RSA (128 bits security) |
| **Side Channel** | Timing attacks | Not mitigated (not critical for demo) |

## Assumptions

✅ **Trusted Channel**: TCP socket (no MITM)  
✅ **Trusted Server**: Private key never leaked  
✅ **Trusted Client**: Stores session key securely  
❌ **Not Protected**: Code execution (sandboxing needed)  

## Future Improvements

1. **TLS/SSL** - Replace TCP + RSA/AES with TLS 1.3
2. **Certificate Pinning** - Verify server identity
3. **HMAC** - Message authentication code
4. **Perfect Forward Secrecy** - Ephemeral session keys
5. **Sandbox** - Isolate code execution

---

# 🧪 Testing & Verification

## Unit Test Example

```java
@Test
public void testHybridEncryptionFlow() {
    // ==================== SETUP ====================
    String plaintext = "print('hello world')";
    
    // Create crypto managers (Client & Server)
    HybridCryptoManager clientCrypto = new HybridCryptoManager(serverPublicKey, null);
    HybridCryptoManager serverCrypto = new HybridCryptoManager(serverPublicKey, serverPrivateKey);
    
    // ==================== PHASE 2: REQUEST ENCRYPT ====================
    HybridEncryptedMessageWithKey clientMsg = clientCrypto.encryptHybridWithKey(plaintext, serverPublicKey);
    
    // Client stores plaintext key
    SecretKey clientSessionKey = clientMsg.getSessionKey();
    String encryptedSessionKey = clientMsg.getEncryptedSessionKey();
    String encryptedRequest = clientMsg.getEncryptedData();
    
    // ==================== PHASE 3: REQUEST DECRYPT ====================
    SecretKey serverSessionKey = serverCrypto.decryptSessionKey(encryptedSessionKey);
    String decryptedRequest = serverCrypto.decryptDataWithAES(encryptedRequest, serverSessionKey);
    
    // Verify
    assertEquals(plaintext, decryptedRequest);
    
    // ==================== PHASE 5: RESPONSE ENCRYPT ====================
    String response = "Output: hello world\n";
    String encryptedResponse = serverCrypto.encryptDataWithAES(response, serverSessionKey);
    
    // ==================== PHASE 6: RESPONSE DECRYPT ====================
    String decryptedResponse = clientCrypto.decryptDataWithAES(encryptedResponse, clientSessionKey);
    
    // Verify
    assertEquals(response, decryptedResponse);
}
```

## Integration Test

```java
@Test
public void testClientServerCommunication() throws Exception {
    // Start Server in thread
    Thread serverThread = new Thread(() -> {
        ServerMain.main(new String[]{});
    });
    serverThread.start();
    
    // Give server time to start
    Thread.sleep(1000);
    
    // Run Client
    String result = ClientService.sendCodeToServer(
        new RequestPayload("print('test')", 92, false)
    );
    
    // Verify
    assertTrue(result.contains("test"));
    
    serverThread.join();
}
```

## Debugging Checklist

- [ ] Public key correctly exchanged
- [ ] AES key correctly generated (32 bytes)
- [ ] RSA encryption/decryption working
- [ ] Base64 encode/decode symmetric
- [ ] Plaintext AES key stored on Client
- [ ] Response encrypted with SAME AES key
- [ ] Request properly decrypted on Server
- [ ] Response properly decrypted on Client

---

## 📚 References

- [RFC 3394: AES Key Wrap Algorithm](https://tools.ietf.org/html/rfc3394)
- [RSA Cryptography: Theory & Practice](https://en.wikipedia.org/wiki/RSA_(cryptosystem))
- [Java Cryptography Architecture (JCA)](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
- [Judge0 API Documentation](https://rapidapi.com/judge0-official/api/judge0-ce)

---

**Version**: 1.0  
**Last Updated**: April 17, 2026  
**Status**: ✅ Complete & Verified
