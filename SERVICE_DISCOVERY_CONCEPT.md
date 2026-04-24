# SERVICE DISCOVERY - KHÁI NIỆM & CÁCH HOẠT ĐỘNG

## 📚 MỤC LỤC
1. [Giới thiệu](#giới-thiệu)
2. [Ý tưởng & Khái niệm](#ý-tưởng--khái-niệm)
3. [Các vấn đề giải quyết](#các-vấn-đề-giải-quyết)
4. [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
5. [Cách hoạt động chi tiết](#cách-hoạt-động-chi-tiết)
6. [Luồng dữ liệu](#luồng-dữ-liệu)
7. [Ưu & nhược điểm](#ưu--nhược-điểm)
8. [Trường hợp sử dụng](#trường-hợp-sử-dụng)

---

## Giới thiệu

**Service Discovery** là một cơ chế tự động để giúp các dịch vụ trong hệ thống phân tán tìm kiếm và kết nối với nhau mà không cần biết địa chỉ cụ thể của từng dịch vụ từ trước.

Trong dự án **LTM-CodeChecker**, chúng ta sử dụng **GitHub Gist** như một "bảng đăng ký dịch vụ" công cộng để:
- Server đăng ký IP:Port của nó
- Client tự động khám phá và kết nối đến Server

---

## Ý tưởng & Khái niệm

### 1. Tại sao lại cần Service Discovery?

#### ❌ **Cách cũ (Hard-coded):**
```
Client cần biết: Server IP = 192.168.1.100, Port = 5000

Vấn đề:
✗ Nếu Server đổi IP → Client bị lỗi
✗ Không linh hoạt trong môi trường cloud/multi-server
✗ Khó mở rộng (scalability)
```

#### ✅ **Cách mới (Service Discovery):**
```
Client tự động tìm kiếm server IP từ bảng đăng ký (GitHub Gist)

Lợi ích:
✓ Server có thể thay đổi IP mà không ảnh hưởng Client
✓ Hỗ trợ load balancing (nhiều server)
✓ Dễ dàng thêm/xóa service
✓ Linh hoạt trong môi trường phân tán
```

### 2. Các mô hình Service Discovery phổ biến

| Mô hình | Cách làm | Ví dụ |
|--------|---------|-------|
| **Client-Side Discovery** | Client tìm kiếm từ registry | Eureka, Consul |
| **Server-Side Discovery** | Load balancer tìm kiếm | Nginx, Kubernetes |
| **Self-Registration** | Service tự đăng ký | ServiceRegistry (LTM) |
| **Third-Party Registration** | Một component khác đăng ký | Container orchestration |

**LTM-CodeChecker sử dụng: Client-Side Discovery + Self-Registration**

---

## Các vấn đề giải quyết

### 1. **"Server đang chạy ở đâu?"**
- **Vấn đề:** Client không biết IP:Port của Server
- **Giải pháp:** Server tự động gửi thông tin lên GitHub Gist khi khởi động
- **Kết quả:** Client có thể lấy thông tin từ Gist bất kỳ lúc nào

### 2. **"Server thay đổi IP thì sao?"**
- **Vấn đề:** Nếu Server khởi động lại ở IP mới, Client bị mất kết nối
- **Giải pháp:** Gist luôn cập nhật IP mới nhất, Client lấy thông tin mỗi khi khởi động
- **Kết quả:** Client tự động kết nối đến Server mới

### 3. **"Nếu Service Discovery thất bại?"**
- **Vấn đề:** GitHub API không phản hồi hoặc mất mạng
- **Giải pháp:** Sử dụng Fallback Address (localhost:5000)
- **Kết quả:** Hệ thống vẫn hoạt động trong môi trường local

### 4. **"Bảo mật thông tin Server IP?"**
- **Vấn đề:** Ai cũng có thể xem Server IP từ public Gist
- **Giải pháp:** Sử dụng private Gist + GitHub Personal Access Token
- **Kết quả:** Chỉ những người có token mới có thể truy cập

---

## Kiến trúc hệ thống

### 📐 **Sơ đồ tổng quan**

```
┌─────────────────────────────────────────────────────────────┐
│                    GITHUB GIST (Registry)                   │
│                    server_registry.txt                       │
│                   Content: IP:PORT                           │
│                   Private/Encrypted                          │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │ (HTTPS)
                  ┌───────────┴───────────┐
                  │                       │
            PUT/PATCH                   GET
        (Đăng ký/Cập nhật)         (Khám phá)
                  │                       │
        ┌─────────▼──────┐      ┌────────▼──────────┐
        │   SERVER       │      │      CLIENT       │
        │  (Port 5000)   │      │   (ClientUI)      │
        │                │      │                   │
        │ ServerMain.java│      │ ClientService.java│
        └────────────────┘      └───────────────────┘
              │                         ▲
              │                         │
              └──────────TCP────────────┘
              (Hybrid Crypto:
               RSA + AES)
```

### 🔌 **Các thành phần chính**

1. **GitHub Gist** - Bảng đăng ký công cộng
2. **ServiceRegistry.java** - Quản lý đăng ký & khám phá
3. **ServerMain.java** - Server khởi động & đăng ký
4. **ClientService.java** - Client khám phá & kết nối

---

## Cách hoạt động chi tiết

### 🔴 **BƯỚC 1: Cấu hình GitHub Gist & Token**

```
User phải:
1. Tạo GitHub Personal Access Token (quyền gist)
2. Tạo GitHub Gist mới (private, tên: server_registry.txt)
3. Cập nhật GIST_ID & GITHUB_TOKEN vào ServiceRegistry.java

Ví dụ:
private static final String GIST_ID = "814c622f74340fe2f5e0b92cf385f95b";
private static final String GITHUB_TOKEN = "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";
```

### 🟡 **BƯỚC 2: Server khởi động & Tự đăng ký**

```
┌──────────────────────────────┐
│ ServerMain.main() được gọi   │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│ 1. Khởi tạo RSA Keys             │
│ 2. Mở ServerSocket(port 5000)    │
│ 3. In "Server listening..."      │
└──────────┬───────────────────────┘
           │
           ▼
┌────────────────────────────────────────────┐
│ ServiceRegistry.registerServer(5000)       │
│                                            │
│ 1. Lấy Local IP: 192.168.1.100            │
│ 2. Tạo serverInfo: "192.168.1.100:5000"   │
│ 3. Gửi PATCH request tới GitHub API:      │
│    PATCH /gists/{GIST_ID}                 │
│    Body: {                                 │
│      "files": {                            │
│        "server_registry.txt": {            │
│          "content": "192.168.1.100:5000"  │
│        }                                   │
│      }                                     │
│    }                                       │
│ 4. GitHub API cập nhật Gist               │
│ 5. In "[+] Đăng ký server thành công!"    │
└────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│ Server sẵn sàng chấp nhận Client │
│ (Vòng lặp accept())              │
└──────────────────────────────────┘
```

### 🟢 **BƯỚC 3: Client khởi động & Tự khám phá**

```
┌──────────────────────────────┐
│ ClientUIFrame.main() được gọi│
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│ ClientService() được khởi tạo    │
│ Gọi discoverServer()             │
└──────────┬───────────────────────┘
           │
           ▼
┌────────────────────────────────────────────┐
│ ServiceRegistry.discoverServer()           │
│                                            │
│ 1. Gửi GET request tới GitHub API:        │
│    GET /gists/{GIST_ID}                   │
│    Header: Authorization: Bearer {TOKEN}  │
│ 2. GitHub API trả về Gist JSON            │
│ 3. Parse và lấy content từ                │
│    files.server_registry.txt.content      │
│ 4. Kết quả: "192.168.1.100:5000"          │
│ 5. In "[+] Khám phá server thành công!"   │
└──────────┬───────────────────────────────┘
           │
           ▼
┌────────────────────────────────────────────┐
│ Parse serverInfo: "192.168.1.100:5000"    │
│                                            │
│ serverIP = "192.168.1.100"                │
│ serverPort = 5000                         │
└──────────┬───────────────────────────────┘
           │
           ▼
┌────────────────────────────────────────────┐
│ Client kết nối tới Server qua TCP socket  │
│ Socket("192.168.1.100", 5000)             │
│                                            │
│ → Bắt đầu Hybrid Crypto handshake         │
│ → Gửi code yêu cầu kiểm tra               │
│ → Nhận kết quả từ server                  │
└────────────────────────────────────────────┘
```

---

## Luồng dữ liệu

### 📊 **Luồng dữ liệu toàn bộ**

```
1. CẤU HÌNH BAN ĐẦU
   ┌──────────────────────────────────────────┐
   │ 1.1: Tạo GitHub Gist trên gist.github.com│
   │ 1.2: Lấy Gist ID từ URL                  │
   │ 1.3: Tạo GitHub Personal Access Token    │
   │ 1.4: Cập nhật ServiceRegistry.java       │
   │       - GIST_ID                          │
   │       - GITHUB_TOKEN                     │
   └──────────────────────────────────────────┘

2. SERVER KHỞI ĐỘNG
   ┌──────────────────────────────────────────┐
   │ ServerMain.main()                        │
   │   ↓                                       │
   │ KeyManager.initializeKeys()              │
   │   ↓                                       │
   │ ServerSocket(5000)                       │
   │   ↓                                       │
   │ ServiceRegistry.registerServer(5000)     │
   │   │                                       │
   │   ├→ getLocalIPAddress() → "192.168.1.100"
   │   │                                       │
   │   ├→ PATCH /gists/{GIST_ID}              │
   │   │   Request:                            │
   │   │   {                                   │
   │   │     "files": {                        │
   │   │       "server_registry.txt": {        │
   │   │         "content": "192.168.1.100:5000"
   │   │       }                               │
   │   │     }                                 │
   │   │   }                                   │
   │   │                                       │
   │   └→ GitHub Gist updated ✓               │
   │   ↓                                       │
   │ Print "[+] Đăng ký server thành công!"   │
   │   ↓                                       │
   │ Loop: accept() và xử lý Client           │
   └──────────────────────────────────────────┘

3. CLIENT KHỞI ĐỘNG
   ┌──────────────────────────────────────────┐
   │ ClientUIFrame.main()                     │
   │   ↓                                       │
   │ ClientService()                          │
   │   ↓                                       │
   │ discoverServer()                         │
   │   │                                       │
   │   ├→ ServiceRegistry.discoverServer()    │
   │   │   │                                   │
   │   │   ├→ GET /gists/{GIST_ID}            │
   │   │   │   Response:                       │
   │   │   │   {                               │
   │   │   │     "files": {                    │
   │   │   │       "server_registry.txt": {    │
   │   │   │         "content": "192.168.1.100:5000"
   │   │   │       }                           │
   │   │   │     }                             │
   │   │   │   }                               │
   │   │   │                                   │
   │   │   └→ return "192.168.1.100:5000"     │
   │   │                                       │
   │   ├→ Parse: serverIP, serverPort         │
   │   │                                       │
   │   └→ Print "[+] Khám phá server thành công!"
   │   ↓                                       │
   │ Stored: this.serverIP, this.serverPort   │
   │   ↓                                       │
   │ ClientUIFrame hiển thị UI                │
   └──────────────────────────────────────────┘

4. CLIENT GỬI YÊU CẦU
   ┌──────────────────────────────────────────┐
   │ User nhập code → Click "Kiểm tra"        │
   │   ↓                                       │
   │ ClientService.sendCodeToServer()         │
   │   │                                       │
   │   ├→ Socket(this.serverIP, this.serverPort)
   │   │   ("192.168.1.100", 5000)            │
   │   │                                       │
   │   ├→ Hybrid Crypto Handshake             │
   │   │   1. Nhận Public Key từ Server       │
   │   │   2. Tạo AES Session Key             │
   │   │   3. Mã hóa AES Key bằng RSA         │
   │   │   4. Gửi encrypted AES Key           │
   │   │                                       │
   │   ├→ Mã hóa request + gửi                │
   │   │   RequestPayload:                     │
   │   │   {                                   │
   │   │     code: "..." (code mã)            │
   │   │     language: "java"                  │
   │   │   }                                   │
   │   │   → Encrypted bằng AES               │
   │   │   → Gửi qua Socket                   │
   │   │                                       │
   │   └→ Nhận + giải mã response             │
   │       ResponsePayload: output, errors    │
   └──────────────────────────────────────────┘

5. SERVER XỬ LÝ YÊU CẦU
   ┌──────────────────────────────────────────┐
   │ ServerSocket.accept()                    │
   │   ↓ (Client kết nối)                     │
   │ ClientHandler(socket, rsaKeyPair)        │
   │   ├→ Hybrid Crypto: Trao đổi key         │
   │   ├→ Nhận + giải mã request              │
   │   ├→ SyntaxChecker / CodeFormatter       │
   │   ├→ OnlineCompilerAPI (nếu cần)         │
   │   ├→ Mã hóa response                     │
   │   └→ Gửi response về Client              │
   └──────────────────────────────────────────┘
```

---

## Ưu & Nhược điểm

### ✅ **Ưu điểm**

| Ưu điểm | Mô tả |
|---------|-------|
| **Linh hoạt** | Server có thể thay đổi IP/Port mà không cần sửa code client |
| **Tự động hóa** | Client tự động tìm kiếm server khi khởi động |
| **Dễ scale** | Có thể thêm nhiều server, client tự động load balance |
| **Không phụ thuộc infrastructure** | Không cần Eureka, Consul - dùng GitHub Gist public |
| **Fallback mechanism** | Nếu discovery thất bại, vẫn có fallback address |
| **Bảo mật** | Sử dụng private Gist + GitHub Token |

### ❌ **Nhược điểm**

| Nhược điểm | Mô tả |
|-----------|-------|
| **Phụ thuộc GitHub** | Nếu GitHub API down, discovery thất bại |
| **Độ trễ network** | Mỗi lần khám phá cần gọi GitHub API (HTTP request) |
| **Không real-time** | Nếu server thay đổi IP, client cũ vẫn cached giá trị cũ |
| **Ít tính năng** | Không có load balancing, health check tự động |
| **Giới hạn GitHub API** | Rate limiting (60 requests/hour không auth) |
| **Một URL duy nhất** | Gist chỉ lưu một IP:Port, không hỗ trợ multiple servers |

---

## Trường hợp sử dụng

### 1. **Phát triển & Testing**
```
Lợi ích: Lập trình viên có thể chạy server trên máy khác nhau,
         client vẫn tự động kết nối
         
Ví dụ:
- Máy A: Chạy server
- Máy B: Chạy client
- Máy C: Thay đổi server code, server restart
- → Client tự động kết nối đến server mới
```

### 2. **Demo & Presentation**
```
Lợi ích: Cách "magic" giúp client tự động tìm server
         Không cần nhắc audience IP address
         
Quy trình:
1. Start Server (ghi IP lên Gist)
2. Start multiple Clients
3. Mỗi Client tự động tìm & kết nối
4. Chạy demo
```

### 3. **Môi trường Multi-location**
```
Lợi ích: Nếu có server ở địa điểm khác nhau,
         cập nhật Gist là đủ, client tìm tự động
         
Ví dụ:
- Server v1 chạy ở Hà Nội: 192.168.1.100:5000
- Server v1 bị down
- Server v2 khởi động ở TP.HCM: 203.0.113.50:5000
- Cập nhật Gist → Tất cả client tự động chuyển sang server v2
```

### 4. **Containerization & Kubernetes**
```
Lợi ích: Container có thể có IP động, service discovery
         giúp client luôn kết nối đúng container
         
Quy trình (Future):
- Docker/K8s start container (IP: 172.17.0.2)
- Container khởi tạo, ghi IP lên Gist
- Client container kết nối tới
- Container scale down → IP khác → Gist cập nhật
- Client tự động kết nối
```

---

## 📝 Tóm tắt

| Khía cạnh | Chi tiết |
|-----------|----------|
| **Khái niệm** | Cơ chế tự động giúp services tìm kiếm nhau |
| **Giải quyết** | Client không cần hard-code IP server |
| **Triển khai** | GitHub Gist + API + ServiceRegistry.java |
| **Luồng** | Server đăng ký → Client khám phá → Kết nối |
| **Ưu điểm** | Linh hoạt, tự động, dễ scale |
| **Nhược điểm** | Phụ thuộc GitHub, độ trễ network |
| **Trường hợp** | Development, Multi-location, Containerization |

---

## 🔗 Tham khảo thêm

- **SERVICE_DISCOVERY_SETUP.md** - Hướng dẫn cấu hình từng bước
- **ServiceRegistry.java** - Mã nguồn cơ chế discovery
- **ServerMain.java** - Cách server khởi động & đăng ký
- **ClientService.java** - Cách client khám phá & kết nối
