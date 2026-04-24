# 📋 DANH SÁCH CÂU HỎI GIÁO VIÊN CÓ THỂ HỎI

**Mục đích**: Giúp sinh viên chuẩn bị trước các câu hỏi phòng vấn từ giáo viên

---

## 📑 MỤC LỤC

1. [Giao Diện GUI (Swing)](#giao-diện-gui-swing)
2. [Mã Hóa Lai (Hybrid Encryption)](#mã-hóa-lai-hybrid-encryption)
3. [Service Discovery](#service-discovery)
4. [Kiến Trúc Server-Client](#kiến-trúc-server-client)
5. [Công Nghệ & API](#công-nghệ--api)SERVICE DISCOVERY
6. [Xử Lý Lỗi & Bảo Mật](#xử-lý-lỗi--bảo-mật)
7. [Multithread & Performance](#multithread--performance)
8. [Cấu Hình & Deployment](#cấu-hình--deployment)
9. [Câu Hỏi Nâng Cao](#câu-hỏi-nâng-cao)

---

## 🎨 Giao Diện GUI (Swing)

### ❓ Câu hỏi cơ bản

**Q1: "Màu sắc code trong text box được tô bằng cách nào?"**
- **Mục đích**: Kiểm tra hiểu biết về syntax highlighting
- **Trả lời**:
  - Sử dụng thư viện **RSyntaxTextArea**
  - Thư viện này tự động nhận diện ngôn ngữ lập trình
  - Áp dụng theme (light/dark) với màu khác nhau cho keywords, strings, comments
  - Được tích hợp sẵn trong `ClientUIFrame.java`
- **Chứng minh**:
  - Mở `ClientUIFrame.java` → dòng: `RSyntaxTextArea codeEditor = new RSyntaxTextArea(...)`
  - Hiển thị code highlighting khi nhập code

**Q2: "Thay đổi ngôn ngữ (Java, Python, C++) ảnh hưởng gì tới editor?"**
- **Trả lời**:
  - Thay đổi syntax highlighting rule của RSyntaxTextArea
  - Cập nhật `languageId` để gửi đúng language tới Judge0 API
  - Ví dụ: Java → languageId=62, Python → languageId=92
- **Code**: `codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA)`

**Q3: "Dark theme và Light theme hoạt động như thế nào?"**
- **Trả lời**:
  - Toggle button thay đổi UIManager look and feel
  - Swing components tự động update lại màu sắc
  - `UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")`
- **Tác dụng**: Giảm mỏi mắt khi lập trình lâu

**Q4: "Vì sao phải sử dụng Swing? Tại sao không dùng JavaFX?"**
- **Trả lời**:
  - Swing: Nhẹ, không cần cài thêm, tương thích Java 21
  - JavaFX: Nặng hơn, cần cài riêng
  - RSyntaxTextArea support Swing tốt hơn
  - Đơn giản, dễ maintain

---

### ❓ Câu hỏi trung bình

**Q5: "Layout GUI được sắp xếp như thế nào?"**
- **Trả lời**:
  - BorderLayout: North (toolbar), Center (editor), South (console)
  - Toolbar: Buttons (Check, Format, Clear, Upload, Theme)
  - Editor: RSyntaxTextArea
  - Console: JTextArea để hiển thị output
- **Hiển thị**: Mở GUI → giải thích các vùng

**Q6: "Cách xử lý khi user nhấn nút 'Kiểm tra'?"**
- **Trả lời**:
  - Lấy code từ editor
  - Lấy language ID từ dropdown
  - Gọi `ClientService.sendCodeToServer(RequestPayload)`
  - Nhận response, parse JSON
  - Hiển thị output/errors ở console
  - Nếu lỗi: highlight dòng lỗi ở editor
- **Luồng**: ClientUIFrame → ClientService → Server → Judge0 → Response

**Q7: "Error highlighting hoạt động thế nào? Làm sao biết dòng nào lỗi?"**
- **Trả lời**:
  - Server nhận code → Judge0 compile
  - Judge0 trả về compile output với line numbers
  - `SyntaxChecker.parseErrors()` extract line numbers từ output
  - ClientService nhận ErrorLog list
  - ClientUIFrame highlight từng dòng lỗi bằng `BadLocationException` + underline đỏ
- **Ví dụ**: `Exception in thread "main" java.lang.NullPointerException at Main.java:10` → highlight dòng 10

**Q8: "Upload file code tới đâu? Cách upload thế nào?"**
- **Trả lời**:
  - Button "Upload" lấy code từ editor
  - **Tùy chọn A**: Lưu local thành file .java/.py
  - **Tùy chọn B**: Upload lên Cloud (GitHub/Drive) - hiện chưa implement
  - **Tùy chọn C**: Lưu cache trên client để lần sau mở lại
- **Hiện tại**: Implement save local hoặc upload GitHub Gist

---

### ❓ Câu hỏi nâng cao

**Q9: "Nếu server disconnect thì GUI phản ứng thế nào?"**
- **Trả lời**:
  - Try-catch block bắt `SocketException` / `ConnectException`
  - Hiển thị error dialog: "Server not available"
  - Suggest: "Check server is running on localhost:5000"
  - Offer fallback: Retry hoặc cache offline
- **Improvement idea**: Show "Connecting..." spinner

**Q10: "Code editor có word wrap không? Syntax validation real-time?"**
- **Trả lời**:
  - RSyntaxTextArea hỗ trợ word wrap (bật toggle)
  - Validation real-time: Check syntax chỉ khi user bấm "Kiểm tra"
  - Improvement: Thêm linter real-time (Checkstyle, ESLint)

---

## 🔐 Mã Hóa Lai (Hybrid Encryption)

### ❓ Câu hỏi cơ bản

**Q11: "Mã hóa lai hoạt động ra sao? Tại sao phải dùng 2 thuật toán?"**
- **Trả lời**:
  - **RSA**: Mã hóa AES key (key exchange)
  - **AES**: Mã hóa dữ liệu (data encryption)
  - **Tại sao**:
    - RSA chậm → không thích hợp cho data lớn
    - AES nhanh → thích hợp cho data lớn
    - RSA asymmetric → không cần shared secret
    - Kết hợp → bảo mật + hiệu suất
- **Ví dụ**:
  ```
  Client: Generate random AES key (256-bit)
          Encrypt AES key with RSA public → gửi
          Encrypt code with AES key → gửi
  
  Server: Receive encrypted AES key
          Decrypt with RSA private → get plaintext AES key
          Decrypt code with AES key
  ```

**Q12: "Tại sao phải tạo random AES key mỗi request?"**
- **Trả lời**:
  - Nếu dùng cùng AES key → dễ bị pattern attack
  - Random key → lại attack khó hơn (không pattern)
  - Mỗi request random → bảo mật cao
- **Ví dụ**: 2 request cùng code, AES key khác → encrypted output khác

**Q13: "Server có private key, tại sao client không có?"**
- **Trả lời**:
  - RSA asymmetric: public key encrypt, private key decrypt
  - **Server**:
    - Private key lưu trong `private_key.txt`
    - Chỉ server biết
    - Dùng để decrypt request từ client
  - **Client**:
    - Chỉ có public key (server gửi lần đầu)
    - Không cần private key (chỉ mã hóa, không giải mã RSA)
  - **Lợi**: Server có thể verify requests là thực (sign)

**Q14: "AES key được lưu ở đâu? Bao lâu xóa?"**
- **Trả lời**:
  - AES key: In-memory (RAM), không lưu disk
  - Lifetime: Một request (tạo → gửi → xóa)
  - Session key reuse: Server reuse cùng AES key cho response
  - Lý do reuse: Client đã có plaintext key, không cần decrypt RSA lại
- **Security**: Key destroy sau response

**Q14b: "Mỗi client có key RSA riêng không? Hay tất cả dùng chung?"**
- **Trả lời - HIỆN TẠI**:
  - ❌ **Hiện tại: Tất cả clients dùng CHUNG 1 cặp RSA key**
  - RSA key pair được tạo **1 lần** khi server khởi động
  - Lưu vào: `public_key.txt` (public), `private_key.txt` (private)
  - **Mỗi client kết nối** → nhận public key cũ → mã hóa bằng key chung này
  - **Vấn đề**: Toàn bộ clients share cùng public/private key
- **Trả lời - CẢI TIẾN ĐỀ XUẤT** (ECDH):
  - ✅ Mỗi client connection → sinh unique session key
  - Sử dụng **ECDH (Elliptic Curve Diffie-Hellman)**
  - **Quy trình**:
    ```
    1. Server sinh ECDH key pair khi client connect
    2. Client sinh ECDH key pair
    3. Trao đổi public keys qua network
    4. Compute shared secret (unique cho mỗi connection)
    5. Derive AES key từ shared secret
    6. Mã hóa/giải mã dữ liệu bằng AES key
    ```
  - **Lợi ích**:
    - Mỗi client → unique key (không reuse)
    - Perfect Forward Secrecy (past sessions safe)
    - Performance tốt (~10x nhanh hơn RSA)
    - Modern standard (TLS 1.3 dùng)
- **Hiện tại chưa cải tiến**: Cơ hội tốt để thêm vào phase 2! 🚀
- **Tham khảo**: Xem `KEY_MANAGEMENT_ANALYSIS.md` để biết chi tiết

---

### ❓ Câu hỏi trung bình

**Q15: "Luồng 6 bước mã hóa là gì?"**
- **Trả lời** (full workflow):
  ```
  1. KEY EXCHANGE:    Client ← Public Key (Base64)
  2. REQUEST ENCRYPT: Client tạo AES key, encrypt request
  3. REQUEST SEND:    Client → [RSA(AES_key), AES(request)]
  4. REQUEST DECRYPT: Server decrypt RSA → AES key, decrypt AES → request
  5. PROCESSING:      Server xử lý (Judge0, format)
  6. RESPONSE:        Server → [RSA(AES_key_cũ), AES(response)]
  7. RESPONSE DECRYPT:Client decrypt AES → response (không decrypt RSA)
  ```
- **Key Point**: Reuse AES key ở step 6 → client không có private key để decrypt

**Q16: "Nếu attacker đánh cắp ciphertext (encrypted data) thì sao?"**
- **Trả lời**:
  - Ciphertext = RSA(AES_key) + AES(data)
  - Attacker không có private key → không decrypt RSA
  - Attacker không biết AES key → không decrypt AES
  - Result: Data vẫn bảo mật
- **Mitigations**: 
  - HTTPS (encrypt transport layer)
  - HMAC (detect tampering)
  - Timestamp (prevent replay)

**Q17: "RSA 2048-bit đủ an toàn không? Có phải upgrade RSA 4096?"**
- **Trả lời**:
  - RSA 2048: An toàn cho 30+ năm (NIST standard)
  - RSA 4096: Gấp 2x chậm, chỉ cần cho TOP SECRET
  - Recommendation: 2048 đủ, upgrade AES 256 thay vì RSA
  - Trade-off: Security vs Performance
- **Current**: RSA 2048 + AES 256 → balanced

**Q18: "Làm sao biết data không bị tamper (modify) giữa đường?"**
- **Trả lời**:
  - Current: Không có HMAC/signature → khó detect tampering
  - Improvement: Thêm HMAC-SHA256
  - Implementation: `HMAC = HMAC_SHA256(AES_key + data)`
  - Server verify: `HMAC_received == HMAC_calculated`

---

### ❓ Câu hỏi nâng cao

**Q19: "Tại sao server phải reuse session key cho response? Nếu tạo AES key mới thì sao?"**
- **Trả lời** (critical point):
  ```
  ❌ WRONG: Server tạo AES key mới
    ├─ Server encrypt AES_key_new với RSA public → ciphertext
    ├─ Send: RSA(AES_key_new) + AES_new(response)
    └─ Client receive:
       ├─ Try decrypt RSA(AES_key_new) với private key
       ├─ ❌ Client không có private key!
       └─ EXCEPTION: "Private key not initialized"

  ✅ RIGHT: Server reuse session key từ request
    ├─ Server already có plaintext AES key
    ├─ Send: RSA(AES_key_old) + AES_old(response)
    └─ Client receive:
       ├─ Skip RSA decrypt (đã có plaintext key)
       ├─ Decrypt AES_old(response) → get response
       └─ ✅ SUCCESS!
  ```
- **Lesson**: Client không có private key → server không thể gửi encrypted AES key mới

**Q20: "Hybrid encryption có perfect forward secrecy (PFS) không?"**
- **Trả lời - HIỆN TẠI**:
  - ❌ Không có PFS
  - Nếu private key RSA bị crack → tất cả sessions compromised (tất cả dùng chung key)
  - Không có key rotation
- **Trả lời - CẢI TIẾN ĐỀ XUẤT** (ECDH):
  - ✅ **Có PFS với ECDH**
  - **Quy trình**:
    ```
    1. Mỗi client connect → sinh unique ECDH key pair
    2. Mỗi connection → compute unique shared secret
    3. Shared secret destroy sau disconnect
    4. ⭐ Attacker crack → chỉ 1 connection compromise
    5. ⭐ Past & future connections vẫn safe
    ```
  - **So sánh**:
    | | Hiện tại (RSA) | ECDH |
    |---|---|---|
    | Per-client key | ❌ | ✅ |
    | PFS | ❌ | ✅ |
    | Performance | ⭐⭐ | ⭐⭐⭐⭐ |
    | Complexity | ⭐⭐ | ⭐⭐⭐ |
  - **Implementation**: Xem `KEY_MANAGEMENT_ANALYSIS.md` 🔐

**Q21: "AES-256-CBC vs AES-256-GCM? Tại sao chọn CBC?"**
- **Trả lời**:
  - **CBC**: Encryption only (giống current)
  - **GCM**: Encryption + Authentication (AEAD)
  - **Tại sao chọn CBC**: Đơn giản, đủ cho demo
  - **Improvement**: Dùng GCM để detect tampering
  - **Note**: HMAC ở Q18 là alternative cho authentication

---

## 🔍 Service Discovery

### ❓ Câu hỏi cơ bản

**Q22: "Client tìm server thế nào? Không phải hard-code IP sao?"**
- **Trả lời**:
  - **Hard-code cũ**: `String server = "192.168.1.100"` → nếu IP thay đổi → lỗi
  - **Service Discovery mới**: 
    - Server khởi động → ghi IP:Port lên GitHub Gist
    - Client khởi động → đọc IP:Port từ GitHub Gist
    - Client kết nối tới IP vừa đọc
  - **Lợi**: Server IP có thể thay đổi → client tự động update

**Q23: "GitHub Gist được dùng làm cái gì?"**
- **Trả lời**:
  - Bảng đăng ký (registry) công cộng
  - File: `server_registry.txt`
  - Content: `192.168.1.100:5000` (server IP:port)
  - Accessed via: GitHub API (REST)
  - Auth: Personal Access Token (private gist)

**Q24: "Server đăng ký IP khi nào? Mỗi lần client request?"**
- **Trả lời**:
  - **Khi nào**: Server khởi động 1 lần
  - **Không phải**: Mỗi request (overhead lớn)
  - **Code location**: `ServerMain.main()` → `ServiceRegistry.registerServer()`
  - **Fallback**: Nếu GitHub API down → client vẫn kết nối localhost:5000

---

### ❓ Câu hỏi trung bình

**Q25: "Nếu server IP thay đổi (e.g., khởi động lại trên máy khác) thì sao?"**
- **Trả lời**:
  - Old client: Vẫn cached old IP → kết nối fail
  - New client: Khởi động → đọc Gist → lấy new IP → kết nối OK
  - **Improvement**: Client periodic refresh (mỗi 5 phút update Gist)
  - **Trade-off**: Refresh vs Network overhead

**Q26: "GitHub API rate limit? Nếu quá nhiều client request sao?"**
- **Trả lời**:
  - Rate limit: 60 requests/hour (không auth), 5000/hour (auth)
  - Current: 1 client request/session → chưa exceed
  - **Improvement**: Cache IP locally + refresh timer
  - **Alternative**: Dùng DNS / ZooKeeper thay GitHub

**Q27: "Private Gist an toàn hơn public Gist chỗ nào?"**
- **Trả lời**:
  - Public: Ai cũng biết server IP (attacker dễ tìm)
  - Private: Cần GitHub Token → chỉ authorized người truy cập
  - Current: Private Gist + Personal Access Token → bảo mật
  - **Note**: Token vẫn hardcoded trong code → production nên dùng env variables

---

### ❓ Câu hỏi nâng cao

**Q28: "Service discovery pattern nào? Client-side hay Server-side?"**
- **Trả lời**:
  - Current: **Client-side discovery** + **Self-registration**
  - **Client-side**: Client tìm registry → lấy server IP
  - **Self-registration**: Server tự đăng ký (không có 3rd party)
  - **Alternatives**:
    - Server-side: Load balancer tìm (Nginx, K8s)
    - 3rd-party: Orchestration tìm (Docker, K8s)

**Q29: "So sánh GitHub Gist vs Eureka vs Consul vs DNS?"**
- **Trả lời**:

| Registry | Pros | Cons | Use Case |
|----------|------|------|----------|
| GitHub Gist | Free, no setup | Rate limit, not real-time | Small projects |
| Eureka | Spring ecosystem, HA | Java only | Spring Boot |
| Consul | Real-time, HA | Complex setup | Production |
| DNS | Standard, fast | Static | Simple |

- **Current project**: GitHub Gist đủ vì demo/educational

**Q30: "Nếu GitHub down thì hệ thống có chạy được không?"**
- **Trả lời**:
  - Fallback: `FALLBACK_SERVER_IP = "localhost"`, `FALLBACK_SERVER_PORT = 5000`
  - **Scenario**:
    - Server & Client cùng local machine → localhost:5000 OK
    - Server & Client khác machine + GitHub down → FAIL
  - **Improvement**: 
    - DNS fallback
    - Local cache (file-based registry)
    - Circuit breaker pattern

---

## 🏗️ Kiến Trúc Server-Client

### ❓ Câu hỏi cơ bản

**Q31: "Kiến trúc Client-Server hoạt động ra sao?"**
- **Trả lời**:
  ```
  CLIENT (GUI)          SERVER (Port 5000)
    │                       │
    ├─ Input code      →    Receive
    │                        │
    ├─ Send request    →    Parse → Judge0
    │                        │
    │                  ←    Process → Response
    │
    └─ Display result   ←   
  ```
- **Communication**: TCP socket (port 5000)
- **Data**: JSON (RequestPayload / ResponsePayload)
- **Security**: Hybrid encryption

**Q32: "Server port 5000 được chọn vì lý do gì? Có thể thay đổi không?"**
- **Trả lời**:
  - 5000: Arbitrary choice (common port dùng development)
  - Có thể thay: `private static final int PORT = 8080` (hoặc port khác)
  - **Yêu cầu**: Port không được sử dụng bởi app khác
  - Check: `netstat -an | find "5000"`
  - Production: Thường 80 (HTTP), 443 (HTTPS), hoặc > 1024

**Q33: "Request / Response payload là gì? Gồm cái gì?"**
- **Trả lời**:
  - **RequestPayload** (Client → Server):
    ```java
    {
      "sourceCode": "print('hello')",      // Code user nhập
      "languageId": 92,                     // Language (92=Python)
      "isFormatOnly": false                 // Format only hay execute?
    }
    ```
  - **ResponsePayload** (Server → Client):
    ```java
    {
      "isSuccess": true,                    // Execution thành công?
      "output": "hello\n",                  // Output
      "formattedCode": "print('hello')",   // Formatted code
      "errors": null                        // Errors (if any)
    }
    ```

**Q34: "Nếu client gửi request nhưng server không response?"**
- **Trả lời**:
  - Socket timeout: Default thường 30s
  - Client hiển thị: "Timeout - Server not responding"
  - Reasons:
    - Server crash
    - Network issue
    - Judge0 API down
  - **Improvement**: Timeout + Retry logic

---

### ❓ Câu hỏi trung bình

**Q35: "Server có thể xử lý multiple clients cùng lúc không?"**
- **Trả lời**:
  - **Yes**, sử dụng multithreading
  - Mỗi client connection → spawn `ClientHandler` thread mới
  - **Code**:
    ```java
    while (true) {
      Socket clientSocket = serverSocket.accept();
      ClientHandler handler = new ClientHandler(clientSocket, rsaKeyPair);
      handler.start();  // ← New thread
    }
    ```
  - **Benefit**: Multiple clients không block nhau
  - **Limitation**: Thread limit (OS default ~1000 threads)

**Q36: "Thread-safety? Có race condition không?"**
- **Trả lời**:
  - RSA key pair: Shared across threads → có thể race condition
  - **Current**: Assume keys immutable (không change after init)
  - **Improvement**:
    - Synchronize access: `synchronized (rsaKeyPair) { ... }`
    - Hoặc use `volatile` keyword
  - **Không cần worry**: Single keyPair, read-only

**Q37: "Connection protocol là gì? Bao nhiêu bước handshake?"**
- **Trả lời**:
  ```
  Step 1: Client connect → TCP
  Step 2: Server send public key
  Step 3: Client receive + validate
  Step 4: Client send encrypted request
  Step 5: Server decrypt + process
  Step 6: Server send encrypted response
  Step 7: Client decrypt + parse
  Step 8: Close connection
  ```
- **Total**: 8 steps, 1 round-trip

**Q38: "Server có lưu request history không? Audit log?"**
- **Trả lời**:
  - Current: Không lưu (memory only)
  - Console output: `[+] Phát hiện Client mới từ: IP:Port`
  - **Improvement**:
    - File-based logging (audit trail)
    - Database (PostgreSQL)
    - Timestamp + client IP + request type
  - **Use**: Analytics, security investigation

---

### ❓ Câu hỏi nâng cao

**Q39: "Nếu server receive garbled/corrupted request thì sao?"**
- **Trả lời**:
  - Try-catch: `catch (JsonSyntaxException)` → invalid JSON
  - Try-catch: `catch (javax.crypto.BadPaddingException)` → decrypt fail
  - Response: Error message + close connection
  - **Improvement**: HMAC/signature (Q18) để detect corruption

**Q40: "Connection pooling? Reuse socket cho multiple requests?"**
- **Trả lời**:
  - Current: 1 socket = 1 request = close
  - **Improvement**: Keep-alive + connection pool
  - **Trade-off**: Complexity vs Performance
  - **HTTP protocol**: Hỗ trợ keep-alive (current socket close immediately)

**Q41: "Scalability - bao nhiêu clients tối đa?"**
- **Trả lời**:
  - **Bottleneck 1**: OS thread limit (~1000-10000)
  - **Bottleneck 2**: Judge0 API rate limit
  - **Bottleneck 3**: Network bandwidth
  - **Improvement**:
    - Thread pool (Executors)
    - Async I/O (NIO)
    - Load balancing (multiple servers)
  - **Current**: ~100-1000 clients OK

---

## 🌐 Công Nghệ & API

### ❓ Câu hỏi cơ bản

**Q42: "Judge0 API là gì? Dùng để làm gì?"**
- **Trả lời**:
  - Online compiler + executor
  - API endpoint: `https://judge0.p.rapidapi.com/submissions`
  - Hỗ trợ 90+ ngôn ngữ lập trình
  - **Use case**: Execute code online mà không cần cài compiler local
  - **Alternative**: Codeforces, HackerRank, LeetCode API

**Q43: "Judge0 API response format?"**
- **Trả lời**:
  ```json
  {
    "token": "abc123",
    "status": {"id": 3, "description": "Accepted"},
    "stdout": "Hello World\n",
    "compile_output": null,
    "exit_code": 0,
    "time": "0.123",
    "memory": "1024"
  }
  ```
- **Status ID**:
  - 1: In Queue
  - 2: Processing
  - 3: Accepted (Success)
  - 4: Wrong Answer
  - 5: Time Limit
  - 6: Compilation Error

**Q44: "RapidAPI là gì? Tại sao phải qua RapidAPI?"**
- **Trả lời**:
  - RapidAPI: API marketplace
  - Judge0: Public API tại https://judge0.com
  - **RapidAPI way**: Proxy + authentication
  - **Alternative**: Direct Judge0 API (free tier có limit)
  - **Current**: Use RapidAPI headers:
    ```
    x-rapidapi-key: {KEY}
    x-rapidapi-host: judge0.p.rapidapi.com
    ```

**Q45: "Code Formatter sử dụng cách nào? Godbolt API?"**
- **Trả lời**:
  - Godbolt: Online compiler + assembler viewer
  - API: `https://godbolt.org/api/format/{language}`
  - **Formatter options**:
    - C/C++: clangformat
    - Python: autopep8
    - Java: google-java-format
  - **Alternative**: Local formatter (Eclipse formatter, Prettier)

---

### ❓ Câu hỏi trung bình

**Q46: "Làm sao retry nếu Judge0 API timeout?"**
- **Trả lời**:
  - Current: 1 try → nếu fail báo lỗi
  - **Improvement**:
    ```java
    for (int i = 0; i < MAX_RETRIES; i++) {
      try {
        return judgeAPI.execute(code);
      } catch (TimeoutException) {
        Thread.sleep(1000 * (i + 1));  // exponential backoff
      }
    }
    ```
  - **Exponential backoff**: 1s, 2s, 4s, 8s
  - **Benefit**: Reduce thundering herd

**Q47: "API rate limiting? Nếu quá nhiều requests tới Judge0?"**
- **Trả lời**:
  - Judge0 free tier: ~200 requests/day
  - RapidAPI: Depends on plan
  - **Handle**:
    - Queue requests
    - Throttle rate (max 1 request/second)
    - Show user: "Too many requests, please wait"
  - **Production**: Upgrade paid plan

**Q48: "Syntax Checker tách lỗi thế nào từ Judge0 output?"**
- **Trả lời**:
  - Judge0 return compile error string
  - Example:
    ```
    error: ';' expected at line 5, column 10
    error: incompatible types at line 12
    ```
  - **Regex parse**:
    ```java
    Pattern pattern = Pattern.compile("line (\\d+)");
    Matcher matcher = pattern.matcher(output);
    while (matcher.find()) {
      int lineNum = Integer.parseInt(matcher.group(1));
      errors.add(new ErrorLog(lineNum, ...));
    }
    ```

**Q49: "Tại sao cần CodeFormatter? User có thể format manual?"**
- **Trả lời**:
  - Manual format: Chậm, lỗi, không consistent
  - Auto format:
    - Consistent style (team standard)
    - Instant (1 click)
    - Correct indentation
  - **Use case**: Code review, submission, learning

**Q50: "RapidAPI key bảo mật thế nào? Có bị leak không?"**
- **Trả lời**:
  - Current: Hardcoded trong code → rủi ro
  - **Lưu**: `src/main/java/com/example/dt7syntaxcheck/server/api/OnlineCompilerAPI.java`
  - **Improvement**:
    - Environment variables: `System.getenv("RAPIDAPI_KEY")`
    - Application properties: `application.properties`
    - GitHub Secrets (CI/CD)
  - **Leak consequence**: Attacker abuse API, extra charges
  - **Mitigation**: Regenerate key, monitor usage

---

### ❓ Câu hỏi nâng cao

**Q51: "Judge0 vs Codeforces API vs HackerRank API - so sánh?"**
- **Trả lời**:

| API | Pros | Cons | Languages |
|-----|------|------|-----------|
| Judge0 | 90+ lang, flexible | Rate limit, free tier | C, C++, Java, Python, JS, etc |
| Codeforces | Reliable, fast | Limited languages | C++, Python, Java |
| HackerRank | Rich problem set | Closed | Similar |
| Local compiler | No rate limit | Setup complex | Depends |

- **Current choice**: Judge0 (flexibility + coverage)

**Q52: "Nếu Judge0 API down thì sao? Fallback?"**
- **Trả lời**:
  - Current: Không có fallback → báo lỗi
  - **Improvement**:
    - Fallback 1: Local compiler (javac, python)
    - Fallback 2: Cached results
    - Fallback 3: Offline mode
  - **Implementation**: Try Judge0 → catch → try local

**Q53: "Asynchronous API calls? Async / Await?"**
- **Trả lời**:
  - Current: Synchronous (blocking) calls
  - **Improvement**: Async calls
    ```java
    CompletableFuture.supplyAsync(() -> judgeAPI.execute(code))
        .thenAccept(result -> updateUI(result));
    ```
  - **Benefit**: UI không freeze khi wait Judge0
  - **Trade-off**: Complexity, error handling

---

## 🛡️ Xử Lý Lỗi & Bảo Mật

### ❓ Câu hỏi cơ bản

**Q54: "Các loại lỗi có thể xảy ra?"**
- **Trả lời**:
  1. **Compilation Error**: Syntax error, undefined variable
  2. **Runtime Error**: NullPointerException, ArrayIndexOutOfBounds
  3. **Network Error**: Connection refused, timeout
  4. **Server Error**: Judge0 crash, file not found
  5. **Encryption Error**: Bad padding, invalid key
- **Handling**: Try-catch, error dialog, log

**Q55: "ErrorLog class chứa thông tin gì?"**
- **Trả lời**:
  ```java
  class ErrorLog {
    int lineNumber;      // Dòng lỗi
    String errorType;    // Compilation / Runtime / etc
    String message;      // Chi tiết lỗi
    String suggestion;   // Hint để fix
  }
  ```

**Q56: "Nếu client input liền 100000 dòng code sao? Có limit không?"**
- **Trả lời**:
  - Current: Không có limit → có thể hang/crash
  - **Improvement**:
    - Max code length: `if (code.length() > 100KB) reject`
    - Max execution time: 10 seconds
    - Max memory: 256 MB
  - **Judge0**: Built-in limits (12 second timeout)

**Q57: "Tại sao phải hash password? Current có user account không?"**
- **Trả lời**:
  - Current: Không có authentication (open to all)
  - **Improvement**:
    - User login: email + password
    - Hash password: bcrypt, scrypt, Argon2
    - Reason: Nếu DB leak → password không readable
  - **Trade-off**: Complexity vs security

---

### ❓ Câu hỏi trung bình

**Q58: "Nếu attacker gửi malicious code (e.g., fork bomb)?"**
- **Trả lời**:
  - Judge0 sandbox: Isolate code execution
  - **Protections**:
    - Memory limit (256 MB)
    - CPU time limit (12 seconds)
    - No system calls (fork, exec)
  - **Result**: Fork bomb bị kill sau vài seconds
  - **Server**: Không bị ảnh hưởng

**Q59: "Input validation - có kiểm tra input không?"**
- **Trả lời**:
  - **Current minimal**:
    - Null check
    - Empty string check
  - **Should add**:
    - Code length limit
    - Language ID validation
    - Special character check
  - **Implementation**: Validator class

**Q60: "Connection closing - bao giờ close socket?"**
- **Trả lời**:
  - **After**: Send response
  - **Code**:
    ```java
    finally {
      socket.close();  // Always close
      stream.close();
    }
    ```
  - **Benefit**: Release resource, prevent leaks
  - **Graceful shutdown**: Send "bye" message before close

**Q61: "SQL injection - có database không?"**
- **Trả lời**:
  - Current: Không dùng database (JSON only)
  - **Nếu thêm DB**:
    - ❌ Wrong: `"SELECT * FROM users WHERE name = '" + username + "'"`
    - ✅ Right: Prepared statement `stmt.setString(1, username)`
  - **No current risk**, but good practice to know

**Q62: "Token expiration - GitHub token hơn 2 năm chưa expire?"**
- **Trả lời**:
  - Current: No expiration set (dangerous!)
  - **Improvement**: Set expiration 90 days
  - **Reason**:
    - If leaked → attacker limited time
    - Force rotate periodically
  - **Action**: GitHub Settings → regenerate key

---

### ❓ Câu hỏi nâng cao

**Q63: "DDoS protection? Nếu attacker spam requests?"**
- **Trả lời**:
  - Current: Không có rate limiting
  - **Improvement**:
    - Rate limit: Max 10 requests/minute/IP
    - IP blocklist
    - CAPTCHA
  - **Implementation**: Guava RateLimiter
  - **Trade-off**: Complexity vs protection

**Q64: "Cryptographic key rotation? Khi nào regenerate RSA keys?"**
- **Trả lời**:
  - Current: Keys generate once, không rotate
  - **Improvement**:
    - Rotate monthly
    - Use Key Management Service (KMS)
  - **How**:
    - Generate new key
    - Sign transition certificate
    - Notify clients
  - **Production**: Use AWS KMS, Azure Key Vault

**Q65: "Man-in-the-middle (MITM) attack? HTTPS sử dụng?"**
- **Trả lời**:
  - Current: Plain TCP (không HTTPS) → vulnerable MITM
  - **Scenario**:
    - Attacker intercept → read plaintext hybrid encryption keys
    - Attacker modify code → server execute wrong code
  - **Mitigation**:
    - ✅ Add HTTPS (TLS)
    - ✅ Certificate pinning
  - **Implementation**: Use SSLSocket instead of Socket

---

## 🚀 Multithread & Performance

### ❓ Câu hỏi cơ bản

**Q66: "Multithread có lợi gì? Tại sao phải thread?"**
- **Trả lời**:
  - Without thread:
    ```
    Client 1 → Server receive → process (5s) → response
    Client 2 → Wait... (blocked)
    ```
  - With thread:
    ```
    Client 1 → Thread 1 receive → process (5s)
    Client 2 → Thread 2 receive → process (5s) [parallel]
    ```
  - **Benefit**: Multiple clients handled simultaneously

**Q67: "Thread safety issue có thể xảy ra ở đâu?"**
- **Trả lời**:
  - **Shared resources**:
    - RSA key pair (read-only OK)
    - Database (if added)
    - File I/O
  - **Race condition**:
    - 2 threads write file cùng lúc
    - Result: Data corrupt
  - **Solution**: Synchronize access

**Q68: "Thread pool vs spawn new thread mỗi request?"**
- **Trả lời**:
  - **Current**: Spawn new (simple)
  - **Thread pool**:
    ```java
    ExecutorService pool = Executors.newFixedThreadPool(10);
    pool.execute(new ClientHandler(...));
    ```
  - **Benefit**: Reuse threads, bounded resource
  - **Trade-off**: Complexity, hardcode pool size

---

### ❓ Câu hỏi trung bình

**Q69: "Context switching overhead? Nếu quá nhiều threads?"**
- **Trả lời**:
  - Context switch: CPU save/restore thread state
  - **Overhead**: ~1-10 microseconds
  - **With 1000 threads**: Significant
  - **Optimization**:
    - Thread pool cap (100-200 threads)
    - Async I/O (Netty, NIO)
  - **Current**: OK for 100 clients

**Q70: "Deadlock có thể xảy ra không?"**
- **Trả lời**:
  - Deadlock: Thread A wait B, B wait A (circular)
  - **Current**: No shared locks → no deadlock
  - **If added shared resource**:
    ```
    Thread 1: lock(A) → wait lock(B)
    Thread 2: lock(B) → wait lock(A)  ❌ DEADLOCK
    ```
  - **Prevention**: Always acquire in same order

---

### ❓ Câu hỏi nâng cao

**Q71: "AsyncIO (NIO) vs Blocking IO (OIO)?"**
- **Trả lời**:
  - **BIO (Blocking)**: Current, 1 thread per client
  - **NIO (Non-blocking)**: 1 thread handle multiple clients
  - **Scalability**:
    - BIO: ~1000 clients max
    - NIO: ~10,000+ clients
  - **Framework**: Netty (NIO), Tomcat (BIO/NIO)
  - **Trade-off**: NIO more complex

**Q72: "Performance bottleneck? Profiling tools?"**
- **Trả lời**:
  - **Bottleneck**: Likely Judge0 API (external dependency)
  - **Profiling tools**:
    - JProfiler
    - YourKit
    - JFR (Java Flight Recorder)
  - **Metric**: CPU, Memory, I/O, Network
  - **Optimization**: Cache, batch requests

---

## 📦 Cấu Hình & Deployment

### ❓ Câu hỏi cơ bản

**Q73: "Cách build project?"**
- **Trả lời**:
  ```bash
  mvn clean compile   # Compile
  mvn package        # Package JAR
  mvn test           # Run tests
  ```
- **Output**: `target/` folder

**Q74: "Cách chạy Server và Client?"**
- **Trả lời**:
  ```bash
  # Terminal 1 - Server
  java -cp target/classes com.example.dt7syntaxcheck.server.ServerMain
  
  # Terminal 2 - Client
  java -cp target/classes com.example.dt7syntaxcheck.client.ClientUIFrame
  ```
- **Requirement**: Java 21 installed

**Q75: "Dependency gồm cái gì?"**
- **Trả lời**:
  - **pom.xml** lists:
    ```xml
    <dependency>
      <groupId>com.fifesoft</groupId>
      <artifactId>rsyntaxtextarea</artifactId>  <!-- Syntax highlighting -->
      <version>3.1.4</version>
    </dependency>
    <!-- Others: OkHttp, Gson, etc -->
    ```
  - Maven download tự động

**Q76: "Port 5000 sudah dipakai, bisa ubah ke port lain tidak?"**
- **Trả lời**:
  - Edit `ServerMain.java`:
    ```java
    private static final int PORT = 8080;  // Change 5000 → 8080
    ```
  - Recompile & rerun
  - Client automatic discover via GitHub Gist (no change needed)

---

### ❓ Câu hỏi trung bình

**Q77: "Deployment ke cloud (AWS, Azure)?"**
- **Trả lời**:
  - **Option 1**: EC2 instance (AWS)
    - Launch VM, install Java, clone repo, run
  - **Option 2**: Container (Docker)
    - Create Dockerfile, push ECR/DockerHub
    - Run container
  - **Option 3**: Serverless (AWS Lambda)
    - Not suitable (long-running)
  - **Recommendation**: Docker + Kubernetes

**Q78: "Docker setup? Dockerfile example?"**
- **Trả lời**:
  ```dockerfile
  FROM openjdk:21-jdk
  COPY . /app
  WORKDIR /app
  RUN mvn clean package -DskipTests
  EXPOSE 5000
  CMD ["java", "-cp", "target/classes", ...]
  ```

**Q79: "Environment variables cấu hình?"**
- **Trả lời**:
  ```bash
  export GITHUB_GIST_ID="814c622f..."
  export GITHUB_TOKEN="ghp_..."
  export RAPIDAPI_KEY="..."
  ```
- **Usage**:
  ```java
  String gistId = System.getenv("GITHUB_GIST_ID");
  ```

**Q80: "Logging setup? Console vs File?"**
- **Trả lời**:
  - Current: System.out/err (console only)
  - **Improvement**:
    - Log4j, SLF4j (logging frameworks)
    - Levels: DEBUG, INFO, WARN, ERROR
    - File output: `server.log`, `client.log`

---

### ❓ Câu hỏi nâng cao

**Q81: "CI/CD pipeline? GitHub Actions setup?"**
- **Trả lời**:
  ```yaml
  name: Build & Test
  on: [push, pull_request]
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v2
        - uses: actions/setup-java@v2
          with:
            java-version: '21'
        - run: mvn clean test
  ```
- **Benefit**: Auto test on every push

**Q82: "Production vs Development configuration?"**
- **Trả lời**:
  - **Dev**: 
    - localhost, port 5000
    - Debug logging
    - No auth
  - **Prod**:
    - HTTPS, custom domain
    - Minimal logging
    - Authentication + rate limiting
  - **Profiles**: `application-dev.properties`, `application-prod.properties`

**Q83: "Monitoring & Alerting?"**
- **Trả lời**:
  - **Metrics**: CPU, Memory, request latency
  - **Tools**: Prometheus, Grafana, ELK
  - **Alerts**: Email if CPU > 80%
  - **Implementation**: Add metrics collection

---

## 💡 Câu Hỏi Nâng Cao

### ❓ Sáng Tạo & Cải Tiến

**Q84: "Tại sao chọn Java? Có thể dùng Python/C# không?"**
- **Trả lời**:
  - **Java**: Cross-platform, strong typing, mature ecosystem
  - **Python**: Simpler, but slower for GUI
  - **C#**: Good, but Windows-only (without .NET Core)
  - **Choice**: Java 21 → modern features, LTS support

**Q85: "Improvement idea gì có thể thêm?"**
- **Trả lời**:
  1. **Offline mode**: Local compiler (javac, python)
  2. **Collaboration**: Multiple users real-time (WebSocket)
  3. **AI integration**: ChatGPT explain errors
  4. **Performance**: Async API calls, caching
  5. **UX**: Drag-drop, autocomplete, themes
  6. **Testing**: Unit tests, integration tests
  7. **Deployment**: Docker, Kubernetes, GitHub Actions

**Q86: "Biggest challenge in implementing this project?"**
- **Trả lời**:
  1. **Encryption**: Understanding RSA + AES hybrid
  2. **Networking**: Client-server architecture
  3. **API integration**: Judge0 rate limiting
  4. **GUI**: Swing complexity (threading + UI updates)
  5. **Testing**: Multi-machine setup

**Q87: "What would you do differently if you start over?"**
- **Trả lời**:
  1. Start with architecture design (diagram)
  2. Use logging framework from start (not System.out)
  3. Add unit tests early (TDD)
  4. Use async API calls (non-blocking)
  5. Better error handling
  6. Configuration file (not hardcoded)

**Q88: "Scalability roadmap?"**
- **Trả lời**:
  - **Phase 1** (Current): ~100 users, single server
  - **Phase 2**: Add caching (Redis), async API
  - **Phase 3**: Multiple servers, load balancer
  - **Phase 4**: Microservices (Judge0 wrapper service)
  - **Phase 5**: Cloud-native (Kubernetes)

---

### ❓ Conceptual Questions

**Q89: "Khác nhau giữa compile-time vs runtime error?"**
- **Trả lời**:
  - **Compile-time**: Syntax error, type mismatch (caught during compilation)
  - **Runtime**: NullPointerException, division by zero (caught during execution)
  - **Example**:
    ```
    ❌ Compile error: System.out.println("hello"   // missing )
    ❌ Runtime error: int x = 1/0;                   // exception
    ```

**Q90: "Asymmetric vs Symmetric encryption?"**
- **Trả lời**:
  - **Symmetric (AES)**: Same key encrypt & decrypt
  - **Asymmetric (RSA)**: Public key encrypt, private key decrypt
  - **Use case**:
    - Symmetric: Fast, data encryption
    - Asymmetric: Key exchange, digital signature
  - **Hybrid**: Combine both (current approach)

---

### 🎬 Conclusion

**Q91: "Đồ án này học được cái gì?"**
- **Kỹ năng**:
  - Java advanced (multithreading, encryption, networking)
  - GUI design (Swing)
  - API integration (Judge0, GitHub)
  - Network programming (TCP/IP)
  - Security (hybrid encryption, authentication)

**Q92: "Real-world application? Có công ty nào dùng?"**
- **Trả lời**:
  - LeetCode, HackerRank, CodeChef
  - IDE online (Repl.it, Glitch)
  - Educational platform
  - Code interview platform

**Q93: "Future idea nếu continue project?"**
- **Trả lời**:
  - **Classroom**: Multiple students, teacher dashboard
  - **Competitive**: Leaderboard, contest mode
  - **AI**: Code review, suggestions
  - **Mobile**: iOS/Android version
  - **Enterprise**: Private deployment, HIPAA compliance

---

## 📊 Bảng Tóm Tắt - Chủ Đề Theo Mức Độ

| Mức | Số câu | Chủ đề | Dự kiến |
|-----|--------|--------|---------|
| 🟢 Cơ bản | 1-20 | GUI, Encryption, Service Discovery | 10-15 phút |
| 🟡 Trung bình | 21-60 | Architecture, API, Multithread | 20-30 phút |
| 🔴 Nâng cao | 61-93 | Scalability, Security, Design | 15-20 phút |

---

**💡 Lời khuyên:**
- Chuẩn bị 3-5 "key points" cho mỗi phần
- Vẽ diagram khi giải thích architecture
- Có demo code snippet sẵn
- Nêu trade-offs (complexity vs performance)
- Show enthusiasm & deep understanding! 🚀

