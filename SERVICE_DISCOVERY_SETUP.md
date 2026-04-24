# SERVICE DISCOVERY - HƯỚNG DẪN CẤU HÌNH

## 📚 Mục đích

Hệ thống sử dụng **Service Discovery** để:
- **Server**: Tự động đăng ký IP + Port lên GitHub Gist khi khởi động
- **Client**: Tự động khám phá Server IP từ GitHub Gist để kết nối

---

## 🔍 CÁCH HOẠT ĐỘNG - TÓM TẮT

### Quy trình đơn giản (3 bước)

```
1. SERVER KHỞI ĐỘNG
   └─ Gọi ServiceRegistry.registerServer()
      └─ Gửi PATCH request lên GitHub Gist
         └─ Cập nhật: server_registry.txt = "192.168.1.100:5000"

2. CLIENT KHỞI ĐỘNG
   └─ Gọi ServiceRegistry.discoverServer()
      └─ Gửi GET request lên GitHub Gist
         └─ Lấy: "192.168.1.100:5000"

3. CLIENT KẾT NỐI
   └─ Socket(192.168.1.100, 5000)
      └─ Bắt đầu Hybrid Crypto handshake
         └─ Gửi/nhận dữ liệu
```

### Kiến trúc chi tiết

```
┌─────────────────────────────────────────────────────┐
│          GITHUB GIST (Public Registry)              │
│  URL: gist.github.com/username/{GIST_ID}           │
│  File: server_registry.txt                         │
│  Content: IP:PORT (được server cập nhật)           │
│  Auth: Personal Access Token                       │
└─────────────────────────────────────────────────────┘
                    ▲                  ▲
                    │ GET (Client)     │ PATCH (Server)
                    │ (READ)           │ (WRITE)
          ┌─────────┘                  └──────┐
          │                                    │
    ┌─────▼──────────┐            ┌──────────▼─────┐
    │  CLIENT        │            │  SERVER        │
    │ ClientService  │            │ ServerMain     │
    │  Port: xxx     │            │ Port: 5000     │
    │                │◄──TCP──────►│                │
    │ (khám phá)     │ Hybrid Crypto (kết nối)    │
    └────────────────┘            └────────────────┘
```

---

## 🔐 CÁCH HOẠT ĐỘNG - CẬP MỨC HTTP

### SERVER: Đăng ký IP (registerServer)

**1. Server lấy IP địa chỉ của máy**
```java
String localIP = getLocalIPAddress();  // "192.168.1.100"
int port = 5000;
String serverInfo = "192.168.1.100:5000";
```

**2. Server tạo JSON payload**
```json
{
  "files": {
    "server_registry.txt": {
      "content": "192.168.1.100:5000"
    }
  },
  "description": "Server IP Registry"
}
```

**3. Server gửi PATCH request đến GitHub API**
```
PATCH https://api.github.com/gists/{GIST_ID}

Headers:
  Authorization: Bearer {GITHUB_TOKEN}
  Accept: application/vnd.github+json
  Content-Type: application/json

Body:
{
  "files": {
    "server_registry.txt": {
      "content": "192.168.1.100:5000"
    }
  },
  "description": "Server IP Registry"
}
```

**4. GitHub API response (thành công)**
```json
{
  "status": 200,
  "message": "Gist updated successfully",
  "files": {
    "server_registry.txt": {
      "content": "192.168.1.100:5000"
    }
  }
}
```

### CLIENT: Khám phá Server (discoverServer)

**1. Client gửi GET request**
```
GET https://api.github.com/gists/{GIST_ID}

Headers:
  Authorization: Bearer {GITHUB_TOKEN}
  Accept: application/vnd.github+json
```

**2. GitHub API response (JSON)**
```json
{
  "url": "https://api.github.com/gists/{GIST_ID}",
  "files": {
    "server_registry.txt": {
      "filename": "server_registry.txt",
      "content": "192.168.1.100:5000"
    }
  },
  "owner": {...},
  "created_at": "...",
  "updated_at": "..."
}
```

**3. Client parse response**
```java
String serverInfo = "192.168.1.100:5000";
String[] parts = serverInfo.split(":");
String serverIP = parts[0];     // "192.168.1.100"
int serverPort = Integer.parseInt(parts[1]);  // 5000
```

---

## ⚙️ Giới thiệu

Hệ thống đã được triển khai với **Service Discovery** dựa trên GitHub Gist:
- **Server**: Khi khởi động, server sẽ gửi IP + Port của nó lên GitHub Gist
- **Client**: Khi khởi động, client sẽ lấy server IP từ GitHub Gist để kết nối

## BƯỚC 1: Tạo GitHub Personal Access Token

### 1.1 Truy cập GitHub Settings
1. Đăng nhập vào GitHub (https://github.com)
2. Click vào avatar → **Settings**
3. Scroll xuống → click **Developer settings**
4. Click **Personal access tokens** → **Tokens (classic)**

### 1.2 Tạo Token mới
1. Click **Generate new token** → **Generate new token (classic)**
2. Điền thông tin:
   - **Note**: `LTM-CodeChecker Service Registry`
   - **Expiration**: `No expiration` (hoặc chọn thời hạn tùy ý)
3. **Chọn quyền (scopes)**:
   - ✅ `gist` - cho phép tạo, đọc, cập nhật gists
4. Click **Generate token**
5. **Copy token vừa tạo** (chỉ hiển thị 1 lần!)

📌 Ví dụ token: `ghp_1234567890abcdefghijklmnopqrstuvwxyz`

---

## BƯỚC 2: Tạo Gist lưu trữ Server IP

### 2.1 Tạo Gist trên GitHub
1. Truy cập https://gist.github.com/
2. Click **Create a new gist**
3. Điền:
   - **Filename**: `server_registry.txt`
   - **Description**: `Server IP Registry for LTM-CodeChecker`
   - **Content**: `127.0.0.1:5000` (placeholder, sẽ được cập nhật bởi server)
4. Click **Create gist** (chọn **Private**)
5. **Copy Gist ID** từ URL
   - Ví dụ URL: `https://gist.github.com/username/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p`
   - **Gist ID**: `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p`

---

## BƯỚC 3: Cấu hình ServiceRegistry

Mở file: `src/main/java/com/example/dt7syntaxcheck/share/ServiceRegistry.java`

### 3.1 Thay đổi các hằng số

Tìm dòng 18-20:
```java
private static final String GIST_ID = "YOUR_GIST_ID_HERE";
private static final String GITHUB_TOKEN = "YOUR_GITHUB_TOKEN_HERE";
private static final String GIST_FILENAME = "server_registry.txt";
```

**Thay đổi thành:**
```java
private static final String GIST_ID = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p";  // ID Gist của bạn
private static final String GITHUB_TOKEN = "ghp_1234567890abcdefghijklmnopqrstuvwxyz";  // Token của bạn
private static final String GIST_FILENAME = "server_registry.txt";
```

💡 **Lưu ý bảo mật**: Trong môi trường production, hãy sử dụng:
- Environment variables: `System.getenv("GITHUB_GIST_ID")`
- Tệp cấu hình: `config.properties`
- GitHub Secrets (nếu dùng CI/CD)

---

## BƯỚC 4: Biên dịch và chạy

### 4.1 Biên dịch project
```bash
mvn clean compile
```

### 4.2 Chạy Server

Trên **Máy 1** (Server):
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.server.ServerMain"
```

**Kết quả mong đợi:**
```
=================================================
   SERVER KIỂM TRA VÀ THỰC THI CODE   
=================================================
Đang khởi động hệ thống...
[INFO] RSA keys đã khởi tạo thành công!

[INFO] Server đang lắng nghe kết nối tại port 5000...

[INFO] Đang đăng ký server IP lên GitHub Gist...
[+] Đăng ký server thành công!
[+] Server IP đã được lưu tại GitHub Gist: 192.168.1.100:5000
```

### 4.3 Chạy Client

Trên **Máy 2** (Client) sau vài giây:
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.client.ClientUIFrame"
```

**Kết quả mong đợi (console):**
```
[CLIENT] Đang khám phá server...
[+] Khám phá server thành công!
[+] Server IP: 192.168.1.100:5000
```

---

## BƯỚC 5: Kiểm tra

### 5.1 Xác nhận Server IP được lưu
1. Truy cập Gist của bạn: `https://gist.github.com/username/GIST_ID`
2. Nội dung file `server_registry.txt` phải là IP:Port của server
3. Ví dụ: `192.168.1.100:5000`

### 5.2 Client kết nối thành công
1. Client sẽ tự động khám phá server từ Gist
2. Không cần thay đổi IP hardcoded
3. Có thể chạy server trên máy khác và client sẽ tự tìm

---

## BƯỚC 6: Triển khai trên 2 máy khác nhau

### Cấu hình cần thiết:
1. **Cả 2 máy** cần cài Java 21 + Maven
2. **Cả 2 máy** cần clone project hoặc copy code
3. **Cả 2 máy** cùng sử dụng **cùng một Gist ID** và **cùng GitHub Token**
4. **Port 5000** trên server phải mở (firewall)

### Các bước:
```
Máy 1 (Server):
1. Cấu hình ServiceRegistry.java (GIST_ID, GITHUB_TOKEN)
2. Chạy: mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.server.ServerMain"
3. Đợi message: "[+] Đăng ký server thành công!"

Máy 2 (Client):
1. Cấu hình ServiceRegistry.java (GIST_ID, GITHUB_TOKEN) - PHẢI GIỐNG máy 1
2. Chạy: mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.client.ClientUIFrame"
3. Client sẽ tự động khám phá server từ GitHub Gist
```

---

## KHẮC PHỤC SỰ CỐ - CẤP ĐỘ TECHNICAL

### ❌ Client không tìm thấy server

#### Lỗi 1: HTTP 401 Unauthorized
```
Response: {"message": "Bad credentials"}
```
**Nguyên nhân**: GitHub Token sai hoặc hết hạn  
**Giải pháp**: 
- Kiểm tra token trong ServiceRegistry.java
- Tạo token mới trên GitHub Settings

#### Lỗi 2: HTTP 404 Not Found
```
Response: {"message": "Not Found"}
```
**Nguyên nhân**: GIST_ID sai  
**Giải pháp**:
- Kiểm tra GIST_ID từ URL: https://gist.github.com/username/{GIST_ID}
- Cập nhật ServiceRegistry.java

#### Lỗi 3: Network timeout
```
Exception: java.net.SocketTimeoutException
```
**Nguyên nhân**: GitHub API không phản hồi hoặc mất internet  
**Giải pháp**:
- Client sẽ fallback về "localhost:5000"
- Kiểm tra internet connection
- Server phải chạy local hoặc ở IP fallback

#### Lỗi 4: Connection refused
```
Exception: java.net.ConnectException: Connection refused
```
**Nguyên nhân**: Server chưa chạy hoặc Port 5000 bị chặn  
**Giải pháp**:
- Kiểm tra server đã khởi động chưa
- Kiểm tra firewall có chặn port 5000 không
- Chạy: `netstat -an | find "5000"` (Windows)

### ❌ Server không đăng ký được IP

#### Lỗi 1: HTTP 422 Unprocessable Entity
```
Response: {"message": "Validation failed"}
```
**Nguyên nhân**: JSON payload không đúng format  
**Giải pháp**:
- Kiểm tra format JSON trong registerServer()
- Tên file phải đúng: "server_registry.txt"

#### Lỗi 2: HTTP 403 Forbidden
```
Response: {"message": "API rate limit exceeded"}
```
**Nguyên nhân**: Vượt quá giới hạn API (60 request/giờ)  
**Giải pháp**:
- Chỉ khởi động server 1 lần
- Chờ 1 giờ hoặc tạo token mới

### ❌ Kết nối giữa Server & Client bị cắt

#### Lỗi: java.net.SocketException: Connection reset
```
Exception: java.net.SocketException: Connection reset by peer
```
**Nguyên nhân**: Network không ổn định, firewall chặn  
**Giải pháp**:
- Kiểm tra firewall cho phép port 5000
- Kiểm tra kết nối network
- Nếu cross-network, kiểm tra routing

---

## 🔒 BẢNG ĐIỀU KHIỂN - ĐẶT LỊCH KIỂM TRA

### Kiểm tra hàng ngày

| Bước | Mục đích | Câu lệnh |
|------|---------|---------|
| 1 | Kiểm tra token hạn | Truy cập GitHub Settings |
| 2 | Kiểm tra Gist có valid | curl https://api.github.com/gists/{GIST_ID} |
| 3 | Kiểm tra IP server | Vào Gist xem nội dung file |
| 4 | Kiểm tra log | Xem console output của Server/Client |

### Monitoring (nếu triển khai production)

```bash
# Theo dõi Gist cập nhật
watch -n 5 'curl -H "Authorization: Bearer {TOKEN}" https://api.github.com/gists/{GIST_ID} | grep -o "192\.168\.[0-9]\+\.[0-9]\+"'

# Log server khởi động
mvn exec:java ... 2>&1 | tee server.log

# Kiểm tra port
netstat -an | find "5000"
```

---

## KHẮC PHỤC SỰ CỐ

### ❌ Client không tìm thấy server
- ✅ Kiểm tra: GIST_ID, GITHUB_TOKEN có đúng?
- ✅ Kiểm tra: Server đã chạy và đăng ký IP chưa?
- ✅ Kiểm tra: Gist có chứa IP:Port không? (truy cập GitHub)
- ✅ Kiểm tra: Token có hết hạn không?

### ❌ Server không đăng ký được IP
- ✅ Kiểm tra: GitHub Token có đúng không?
- ✅ Kiểm tra: GIST_ID có đúng không?
- ✅ Kiểm tra: Token có quyền `gist` không?
- ✅ Kiểm tra: Internet connection?

### ❌ Connection timeout
- ✅ Server phải chạy trước Client
- ✅ Port 5000 phải mở trên server (firewall)
- ✅ 2 máy phải cùng network hoặc có routing đúng

---

## TƯƠNG LAI: Các lựa chọn khác

Ngoài GitHub Gist, bạn có thể sử dụng:

### 1️⃣ GitHub Releases (miễn phí)
- Lưu IP trong file text trong release

### 2️⃣ Firebase Realtime Database
- Cơ sở dữ liệu real-time, dễ cập nhật

### 3️⃣ AWS S3 / Azure Blob Storage
- Dịch vụ cloud chuyên dụng

### 4️⃣ DNS Service (Route 53, CloudFlare)
- Quản lý DNS subdomain

### 5️⃣ ZooKeeper / etcd
- Service discovery chuyên dụng cho microservices

---

## CÁCH TRIỂN KHAI ĐƠN GIẢN HƠN (Nếu không muốn dùng GitHub)

Tạo `ServiceRegistry.java` phiên bản đơn giản:

```java
// Phiên bản đơn giản: Hard-code IP server
public class ServiceRegistry {
    public static String discoverServer() {
        return "192.168.1.100:5000";  // IP server cố định
    }
}
```

Cách này dễ nhưng không có tính "dynamic" - nếu server IP thay đổi phải cập nhật code.

---

## TÓM TẮT

| Thành phần | Mô tả |
|-----------|-------|
| **ServiceRegistry.java** | Quản lý đăng ký/khám phá server IP qua GitHub Gist |
| **ServerMain.java** | Gọi `ServiceRegistry.registerServer()` khi khởi động |
| **ClientService.java** | Gọi `ServiceRegistry.discoverServer()` để lấy server IP |
| **Nơi lưu trữ** | GitHub Gist (đáng tin cậy, miễn phí) |

✅ **Ưu điểm:**
- Không cần cấu hình cố định IP
- Server có thể chạy trên bất kỳ máy nào
- Client tự động tìm server
- Bảo mật (sử dụng HTTPS + Token)
- Mở rộng dễ dàng

---

Tài liệu cấu hình hoàn chỉnh! Hãy làm theo từng bước để triển khai trên 2 máy.
