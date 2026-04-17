# Hướng Dẫn Build, Compile, và Test

## 📋 Yêu Cầu Hệ Thống

- **Java 21** hoặc cao hơn
- **Maven 3.6** hoặc cao hơn
- **Git** (optional)

## 🔧 Cài Đặt Maven

### Windows

#### Option 1: Cài đặt Maven qua Chocolatey
```powershell
choco install maven
```

#### Option 2: Tải Maven thủ công
1. Truy cập: https://maven.apache.org/download.cgi
2. Tải `apache-maven-3.9.x-bin.zip`
3. Giải nén vào `C:\Program Files\maven`
4. Thêm `C:\Program Files\maven\bin` vào **System Environment Variables → PATH**
5. Mở PowerShell mới, kiểm tra:
```powershell
mvn -version
```

### macOS
```bash
brew install maven
mvn -version
```

### Linux
```bash
sudo apt install maven
mvn -version
```

---

## 🏗️ Build Dự Án

### Bước 1: Mở Terminal / PowerShell

```powershell
# Điều hướng tới thư mục dự án
cd d:\LTM-codechecker

# Hoặc trên Linux/macOS:
cd /path/to/LTM-codechecker
```

### Bước 2: Clean và Compile

```bash
# Xóa build cũ + Compile project
mvn clean compile

# Output mong đợi:
# [INFO] Building dt7syntaxcheck 1.0-SNAPSHOT
# [INFO] --------------------------------[ jar ]--------------------------------
# [INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ dt7syntaxcheck ---
# [INFO] Deleting d:\LTM-codechecker\target
# [INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ dt7syntaxcheck ---
# [INFO] Compiling 15 source files to d:\LTM-codechecker\target\classes
# [INFO] BUILD SUCCESS
```

### Bước 3: Tạo JAR Package

```bash
# Build project thành JAR file
mvn clean package -DskipTests

# Output:
# [INFO] Building JAR: d:\LTM-codechecker\target\dt7syntaxcheck-1.0-SNAPSHOT.jar
# [INFO] BUILD SUCCESS
```

### Bước 4: Kiểm Tra JAR

```powershell
# Kiểm tra JAR đã được tạo
ls target/*.jar

# Expected output:
# Mode                 LastWriteTime         Length Name
# ----                 -------------         ------ ----
# -a---          2026-04-17    10:30      15234567 dt7syntaxcheck-1.0-SNAPSHOT.jar
```

---

## 🚀 Chạy Dự Án

### Option 1: Chạy từ IDE (IntelliJ IDEA / Eclipse)

#### Chạy Server
1. Mở `src/main/java/com/example/dt7syntaxcheck/server/ServerMain.java`
2. Click chuột phải → **Run 'ServerMain.main()'**
3. Hoặc nhấn `Ctrl+Shift+F10` (IntelliJ)

#### Chạy Client
1. Mở `src/main/java/com/example/dt7syntaxcheck/client/ClientUIFrame.java`
2. Click chuột phải → **Run 'ClientUIFrame.main()'**
3. Hoặc nhấn `Ctrl+Shift+F10` (IntelliJ)

### Option 2: Chạy từ Terminal

#### Chạy Server
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.server.ServerMain"

# Output:
# =================================================
#    SERVER KIỂM TRA VÀ THỰC THI CODE   
# =================================================
# Đang khởi động hệ thống...
# [+] Tạo cặp RSA keys mới...
# [+] Public key đã lưu vào: public_key.txt
# [+] Private key đã lưu vào: private_key.txt
# [+] RSA keys đã khởi tạo thành công!
# [INFO] Server đang lắng nghe kết nối tại port 5000...
```

#### Chạy Client (Terminal khác)
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.client.ClientUIFrame"

# Cửa sổ GUI sẽ mở lên
```

### Option 3: Chạy từ JAR (sau khi build)

#### Chạy Server
```bash
java -cp target/dt7syntaxcheck-1.0-SNAPSHOT.jar com.example.dt7syntaxcheck.server.ServerMain
```

#### Chạy Client (Terminal khác)
```bash
java -cp target/dt7syntaxcheck-1.0-SNAPSHOT.jar com.example.dt7sintaxcheck.client.ClientUIFrame
```

---

## 🧪 Test Mã Hóa Lai

### Kiểm Tra Handshake Key Exchange

#### Kịch Bản Test

**Bước 1**: Khởi động Server
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.server.ServerMain"

# Console Server:
# [+] Tạo cặp RSA keys mới...
# [+] Public key đã lưu vào: public_key.txt
# [+] Private key đã lưu vào: private_key.txt
# [+] RSA keys đã khởi tạo thành công!
# [INFO] Server đang lắng nghe kết nối tại port 5000...
```

**Bước 2**: Khởi động Client
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.client.ClientUIFrame"

# Console Client:
# [CLIENT] ✓ Nhận public key từ server (Key Exchange thành công!)
# [CLIENT] ✓ Mã hóa Hybrid thành công!
# [CLIENT] → Session Key (RSA): MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ...
# [CLIENT] → Data (AES): 8P9L2x4q7R3nM5vW1Y8zQ0wS2xU4vX6yZ...
# [CLIENT] ✓ Đã gửi dữ liệu mã hóa tới server
```

**Bước 3**: Console Server nhận được yêu cầu
```
[127.0.0.1:54321] [INFO] Luồng xử lý đã mở. Thực hiện Key Exchange...
[127.0.0.1:54321] [INFO] Đã gửi Public Key cho Client.
[127.0.0.1:54321] [INFO] ✓ Giải mã Hybrid thành công! Ngôn ngữ ID: 62 | Chỉ Format: false
[127.0.0.1:54321] [INFO] Đang đẩy code sang Judge0 API...
[127.0.0.1:54321] [INFO] Code chuẩn xác! Đã format và lấy output.
[127.0.0.1:54321] [INFO] ✓ Đã mã hóa Hybrid và gửi response về Client!
```

**Bước 4**: Console Client nhận response
```
[CLIENT] ✓ Nhận response mã hóa từ server
[CLIENT] ✓ Giải mã Hybrid thành công!
```

### Kiểm Tra File Key

```powershell
# Windows
ls .\public_key.txt
ls .\private_key.txt

# Linux/macOS
ls ./public_key.txt
ls ./private_key.txt

# Nội dung file (Base64, không đọc được trực tiếp)
cat public_key.txt
# Output: MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
```

### Test với Code Java

#### Test 1: Code Hợp Lệ
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

**Kết quả dự kiến**:
- ✓ Không có lỗi
- Output: `Hello, World!`
- Code được định dạng đẹp

#### Test 2: Code Có Lỗi
```java
public class TestError {
    public static void main(String[] args) {
        System.out.println("Missing semicolon")  // Thiếu ;
    }
}
```

**Kết quả dự kiến**:
- ✗ Phát hiện lỗi: `error: ';' expected`
- Hiển thị dòng lỗi: Line 3

### Test với Code Python

#### Test 1: Code Hợp Lệ
```python
def hello():
    print("Hello from Python")

hello()
```

**Kết quả dự kiến**:
- ✓ Không có lỗi
- Output: `Hello from Python`

#### Test 2: Code Có Lỗi
```python
def hello(
    print("Missing closing parenthesis")
```

**Kết quả dự kiến**:
- ✗ Lỗi syntax
- Hiển thị: `"(" was not closed`

---

## 🔍 Troubleshooting

### Lỗi: "Maven not found"

**Giải pháp**:
1. Cài đặt Maven (xem hướng dẫn trên)
2. Thêm Maven vào PATH
3. Khởi động lại Terminal/IDE

### Lỗi: "Java 21 not found"

**Giải pháp**:
```bash
# Kiểm tra Java version
java -version

# Nếu không phải Java 21, cài đặt Java 21
# Windows: https://www.oracle.com/java/technologies/downloads/#java21
# macOS: brew install openjdk@21
# Linux: sudo apt install openjdk-21-jdk
```

### Lỗi: "Port 5000 already in use"

**Giải pháp**:
```bash
# Tìm process sử dụng port 5000
# Windows
netstat -ano | findstr :5000

# Linux/macOS
lsof -i :5000

# Kill process
# Windows: taskkill /PID <PID> /F
# Linux/macOS: kill -9 <PID>
```

### Lỗi: "Connection refused" từ Client

**Giải pháp**:
1. Đảm bảo Server đã khởi động
2. Kiểm tra port 5000 có đúng không (ServerMain.java)
3. Kiểm tra SERVER_IP trong ClientService.java

### Lỗi: "Compile failed" từ Judge0 API

**Giải pháp**:
1. Kiểm tra API key trong OnlineCompilerAPI.java
2. Đảm bảo có kết nối Internet
3. Kiểm tra Judge0 status: https://judge0.com

---

## 📊 Kiểm Tra Build Details

### Xem Dependency Tree
```bash
mvn dependency:tree

# Output:
# com.example:dt7syntaxcheck:jar:1.0-SNAPSHOT
# +- com.google.code.gson:gson:jar:2.10.1:compile
# +- com.squareup.okhttp3:okhttp:jar:4.10.0:compile
# +- org.json:json:jar:20230618:compile
# +- com.formdev:flatlaf:jar:3.2.5:compile
# \- com.fifesoft:rsyntaxtextarea:jar:3.3.0:compile
```

### Xem Build Information
```bash
# Chi tiết build
mvn help:describe -Dplugin=maven-compiler-plugin

# Kiểm tra plugin versions
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-shade-plugin
```

---

## ✅ Checklist Hoàn Thành Build

- [ ] Java 21 đã cài đặt
- [ ] Maven 3.6+ đã cài đặt
- [ ] Maven nằm trong PATH (kiểm tra: `mvn -version`)
- [ ] Điều hướng tới `d:\LTM-codechecker`
- [ ] Chạy `mvn clean compile` thành công
- [ ] Chạy `mvn clean package` thành công
- [ ] JAR file được tạo tại `target/dt7syntaxcheck-1.0-SNAPSHOT.jar`
- [ ] Server khởi động: `mvn exec:java -Dexec.mainClass="...ServerMain"`
- [ ] Client khởi động: `mvn exec:java -Dexec.mainClass="...ClientUIFrame"`
- [ ] Test mã hóa lai: Nhận public key, mã hóa/giải mã dữ liệu thành công
- [ ] Files `public_key.txt` và `private_key.txt` được tạo

---

## 🎯 Lệnh Nhanh

```bash
# Compile
mvn clean compile

# Build JAR
mvn clean package -DskipTests

# Chạy Server
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.server.ServerMain"

# Chạy Client
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.client.ClientUIFrame"

# Kiểm tra dependency
mvn dependency:tree

# Format code (tùy chọn)
mvn spotless:apply

# Run tests
mvn clean test
```

---

**Version**: 1.0  
**Ngày Cập Nhật**: April 17, 2026
