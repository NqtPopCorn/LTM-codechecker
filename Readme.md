# LTM Code Checker - Hệ thống Kiểm tra Cú pháp và Thực thi Code

**LTM Code Checker** là một ứng dụng Java desktop hiện đại, cho phép người dùng kiểm tra cú pháp code, thực thi các đoạn code và định dạng code đẹp cho nhiều ngôn ngữ lập trình khác nhau.

---

## 📋 Mục lục

1. [Tổng quan dự án](#tổng-quan-dự-án)
2. [Công nghệ sử dụng](#công-nghệ-sử-dụng)
3. [Các chức năng chính](#các-chức-năng-chính)
4. [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
5. [Cấu trúc dự án](#cấu-trúc-dự-án)
6. [API và Giao thức trao đổi dữ liệu](#api-và-giao-thức-trao-đổi-dữ-liệu)
7. [Hướng dẫn cài đặt](#hướng-dẫn-cài-đặt)
8. [Hướng dẫn chạy demo](#hướng-dẫn-chạy-demo)
9. [Các lệnh CLI được hỗ trợ](#các-lệnh-cli-được-hỗ-trợ)

---

## 🎯 Tổng quan dự án

**LTM Code Checker** được xây dựng dựa trên mô hình **Client-Server**:
- **Client**: Giao diện GUI hiện đại, cho phép người dùng viết code, chọn ngôn ngữ lập trình, và gửi yêu cầu kiểm tra
- **Server**: Xử lý yêu cầu từ client, thực thi các lệnh kiểm tra cú pháp, và gửi kết quả trở lại client

Dự án tích hợp **Judge0 API** - một nền tảng online compiler nổi tiếng, để hỗ trợ chạy code cho 90+ ngôn ngữ lập trình.

---

## 💻 Công nghệ sử dụng

### Backend (Server)
- **Java 21**: Ngôn ngữ lập trình chính
- **Multithread Socket Programming**: Xử lý nhiều client đồng thời
- **OkHttp3**: Gửi HTTP request đến Judge0 API
- **Gson & JSON**: Xử lý JSON, mã hóa/giải mã dữ liệu
- **Maven**: Quản lý dependency và build project

### Frontend (Client)
- **Swing**: Framework xây dựng giao diện desktop
- **RSyntaxTextArea**: Editor code với syntax highlighting
- **FlatLaf**: Theme modern cho giao diện (Dark/Light mode)
- **OkHttp3**: Giao tiếp với server qua HTTP

### API Bên ngoài
- **Judge0 API**: Online compiler API hỗ trợ 90+ ngôn ngữ lập trình

---

## ✨ Các chức năng chính

### 1. **Kiểm tra Cú pháp (Syntax Checking)**
   - Phát hiện lỗi cú pháp trên từng dòng code
   - Hiển thị thông báo lỗi chi tiết (dòng, cột, loại lỗi)
   - Hỗ trợ các ngôn ngữ: Python, Java, C++, C#, JavaScript, v.v.

### 2. **Thực thi Code (Code Execution)**
   - Chạy code và hiển thị output trực tiếp
   - Hỗ trợ nhập input từ người dùng
   - Xử lý lỗi runtime và hiển thị thông báo lỗi

### 3. **Định dạng Code (Code Formatting)**
   - Tự động căn lề code theo chuẩn của từng ngôn ngữ
   - Làm cho code dễ đọc và theo chuẩn

### 4. **Chọn Ngôn ngữ**
   - Combo box cho phép chọn ngôn ngữ lập trình
   - Tự động cập nhật syntax highlighting khi thay đổi ngôn ngữ

### 5. **Giao diện người dùng**
   - Editor code với syntax highlighting
   - Console output hiển thị kết quả
   - Nút điều khiển: Upload, Check, Format, Clear
   - Toggle Dark/Light Mode

### 6. **Mã hóa dữ liệu (Encryption)**
   - Mã hóa/giải mã dữ liệu trao đổi giữa client-server
   - Bảo mật thông tin code

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT (GUI)                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ClientUIFrame: Giao diện chính                  │   │
│  │  - Code Editor (RSyntaxTextArea)                 │   │
│  │  - Console Output                                │   │
│  │  - Language Selection (Combo Box)                │   │
│  │  - Control Buttons                               │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ClientService: Giao tiếp với Server             │   │
│  │  - Mã hóa/giải mã dữ liệu                        │   │
│  │  - Gửi request tới Server                        │   │
│  │  - Nhận response từ Server                        │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────┬────────────────────────────────────────┘
                 │ Socket Communication (Port 5000)
                 │
┌────────────────▼────────────────────────────────────────┐
│                  SERVER                                 │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ServerMain: Khởi tạo và lắng nghe kết nối      │   │
│  │  - ServerSocket port 5000                        │   │
│  │  - Multithread ClientHandler                     │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ClientHandler: Xử lý mỗi client                 │   │
│  │  - Nhận request từ client                        │   │
│  │  - Gọi SyntaxChecker hoặc OnlineCompilerAPI     │   │
│  │  - Gửi response về client                        │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  SyntaxChecker: Kiểm tra cú pháp local           │   │
│  │  - Parse lỗi từ compiler output                  │   │
│  │  - Hỗ trợ Java, C++, C#, Python                 │   │
│  │  - CodeFormatter: Định dạng code                │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  OnlineCompilerAPI: Gọi Judge0 API               │   │
│  │  - Compile và run code online                    │   │
│  │  - Hỗ trợ 90+ ngôn ngữ                          │   │
│  │  - Mã hóa Base64 source code                     │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────┬────────────────────────────────────────┘
                 │ HTTPS Request
                 │
        ┌────────▼─────────┐
        │  Judge0 API      │
        │ (Online Compiler)│
        └──────────────────┘
```

---

## 📁 Cấu trúc dự án

```
LTM-codechecker/
├── pom.xml                                    # Maven configuration
├── Readme.md                                  # Tài liệu dự án
└── src/
    ├── main/java/com/example/dt7syntaxcheck/
    │   ├── client/
    │   │   ├── ClientUIFrame.java            # Giao diện chính của client
    │   │   └── ClientService.java            # Xử lý kết nối với server
    │   ├── server/
    │   │   ├── ServerMain.java               # Entry point của server
    │   │   ├── ClientHandler.java            # Xử lý từng client connection
    │   │   ├── KeyManager.java               # Quản lý mã hóa/giải mã
    │   │   ├── api/
    │   │   │   └── OnlineCompilerAPI.java    # Gọi Judge0 API
    │   │   └── services/
    │   │       ├── SyntaxChecker.java        # Kiểm tra cú pháp
    │   │       └── CodeFormatter.java        # Định dạng code
    │   └── share/
    │       ├── RequestPayload.java           # Cấu trúc request
    │       ├── ResponsePayload.java          # Cấu trúc response
    │       ├── ErrorLog.java                 # Lưu thông tin lỗi
    │       └── CryptoManager.java            # Mã hóa/giải mã dữ liệu
    └── test/java/                            # Thư mục test (trống)
```

---

## 🔌 API và Giao thức trao đổi dữ liệu

### Request/Response Model

#### **RequestPayload** (Client → Server)
```json
{
  "sourceCode": "public class Hello { ... }",
  "languageId": 62,
  "isFormatOnly": false
}
```

**Tham số:**
- `sourceCode` (String): Mã nguồn cần kiểm tra/chạy
- `languageId` (int): ID của ngôn ngữ lập trình (62=Java, 54=C++, 51=C#, v.v.)
- `isFormatOnly` (boolean): `true` nếu chỉ muốn format code, `false` để chạy/kiểm tra

#### **ResponsePayload** (Server → Client)
```json
{
  "isSuccess": true,
  "output": "Hello World",
  "formattedCode": "public class Hello {\n    public static void main() { ... }\n}",
  "errors": [
    {
      "lineNumber": 7,
      "message": "unclosed string literal"
    }
  ]
}
```

**Tham số:**
- `isSuccess` (boolean): `true` nếu code chạy thành công
- `output` (String): Output từ chương trình hoặc thông báo lỗi gốc
- `formattedCode` (String): Code đã được định dạng (nếu isSuccess = true)
- `errors` (List<ErrorLog>): Danh sách lỗi cú pháp (nếu isSuccess = false)

### Socket Communication

- **Protocol**: TCP/IP
- **Port**: 5000 (configurable)
- **Data Encoding**: JSON over Sockets
- **Encryption**: Custom encryption (CryptoManager)

### Judge0 API Integration

```
POST https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=true&wait=true

Headers:
  X-RapidAPI-Key: [YOUR_API_KEY]
  X-RapidAPI-Host: judge0-ce.p.rapidapi.com
  Content-Type: application/json

Body:
{
  "source_code": "[BASE64_ENCODED_CODE]",
  "language_id": 62
}

Response:
{
  "stdout": "[EXECUTION_OUTPUT]",
  "stderr": "[ERROR_OUTPUT]",
  "compile_output": "[COMPILE_ERROR]",
  "status": { "id": 3, "description": "Accepted" }
}
```

**Lưu ý:** Source code được mã hóa Base64 trước khi gửi đến Judge0.

---

## 🛠️ Hướng dẫn cài đặt

### Yêu cầu hệ thống
- **Java 21** hoặc cao hơn
- **Maven 3.6+**
- **Git** (optional)

### Bước 1: Clone hoặc tải dự án
```bash
git clone https://github.com/NqtPopCorn/LTM-codechecker.git
cd LTM-codechecker
```

### Bước 2: Cải biến môi trường Judge0 API (Optional)
Mở file [src/main/java/com/example/dt7syntaxcheck/server/api/OnlineCompilerAPI.java](src/main/java/com/example/dt7syntaxcheck/server/api/OnlineCompilerAPI.java) và thay thế API key:

```java
private static final String API_KEY = "YOUR_RAPIDAPI_KEY";
```

Để lấy API key:
1. Truy cập https://rapidapi.com/
2. Tìm Judge0 API
3. Subscribe (free tier available)
4. Copy API key từ dashboard

### Bước 3: Build dự án với Maven
```bash
mvn clean install
# hoặc
mvn clean package
```

### Bước 4: Kiểm tra build thành công
Sẽ có file JAR tạo ra tại: `target/dt7syntaxcheck-1.0-SNAPSHOT.jar`

---

## 🚀 Hướng dẫn chạy demo

### Cách 1: Chạy từ IDE (IntelliJ IDEA hoặc Eclipse)

#### Chạy Server
1. Mở file [src/main/java/com/example/dt7syntaxcheck/server/ServerMain.java](src/main/java/com/example/dt7syntaxcheck/server/ServerMain.java)
2. Click chuột phải → **Run 'ServerMain.main()'**
3. Server sẽ khởi động và lắng nghe tại **Port 5000**
4. Output console:
   ```
   =================================================
      SERVER KIỂM TRA VÀ THỰC THI CODE   
   =================================================
   Đang khởi động hệ thống...
   [INFO] Server đang lắng nghe kết nối tại port 5000...
   ```

#### Chạy Client (Giao diện GUI)
1. Mở file [src/main/java/com/example/dt7syntaxcheck/client/ClientUIFrame.java](src/main/java/com/example/dt7syntaxcheck/client/ClientUIFrame.java)
2. Click chuột phải → **Run 'ClientUIFrame.main()'**
3. Cửa sổ GUI sẽ xuất hiện

### Cách 2: Chạy từ Terminal

#### Build dự án
```bash
mvn clean install
```

#### Chạy Server
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.server.ServerMain"
```

#### Chạy Client (trong terminal khác)
```bash
mvn exec:java -Dexec.mainClass="com.example.dt7syntaxcheck.client.ClientUIFrame"
```

### Cách 3: Chạy từ JAR (sau khi build)

#### Chạy Server
```bash
java -cp target/dt7syntaxcheck-1.0-SNAPSHOT.jar com.example.dt7syntaxcheck.server.ServerMain
```

#### Chạy Client
```bash
java -cp target/dt7syntaxcheck-1.0-SNAPSHOT.jar com.example.dt7syntaxcheck.client.ClientUIFrame
```

---

## 📝 Demo thực tế

### Bước 1: Khởi động Server
- Server sẽ chờ kết nối từ client tại port 5000

### Bước 2: Mở Client GUI
- Giao diện sẽ hiển thị với:
  - Code Editor (bên trái)
  - Language Selector (Combo box)
  - Buttons: Upload, Check, Format, Clear, Theme Toggle
  - Console Output (bên dưới)

### Bước 3: Viết Code
Ví dụ 1 - Java code hợp lệ:
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

### Bước 4: Chọn Ngôn ngữ
- Chọn "Java" từ combo box
- Syntax highlighting sẽ cập nhật

### Bước 5: Kiểm tra Cú pháp
- Click nút **"Check"**
- Nếu code hợp lệ: Console sẽ hiển thị output
- Nếu có lỗi: Console sẽ hiển thị danh sách lỗi

### Bước 6: Ví dụ Code có Lỗi
```java
public class Test {
    public static void main(String[] args) {
        System.out.println("Missing semicolon")
    }
}
```
- Console sẽ hiển thị: `error: ';' expected`

### Bước 7: Định dạng Code
- Click nút **"Format"**
- Code sẽ được tự động căn lề và định dạng đẹp

### Bước 8: Clear Console
- Click nút **"Clear"** để xóa output

### Bước 9: Thay đổi Theme
- Click nút **"Theme Toggle"** để chuyển giữa Dark Mode và Light Mode

---

## 🔧 Các lệnh CLI được hỗ trợ

### Python

**Kiểm tra Cú pháp:**
```bash
python -m pyright a.py
```

**Chạy Code:**
```bash
python a.py
```

**Ví dụ Output (Lỗi):**
```
  \a.py:2:6 - error: "(" was not closed
  \a.py:2:7 - error: String literal is unterminated
3 errors, 0 warnings, 0 informations
```

---

### Java

**Kiểm tra Cú pháp / Chạy:**
```bash
java TestFile.java
```

**Ví dụ Output (Lỗi):**
```
TestFile.java:7: error: unclosed string literal
        System.out.println("Error);
                           ^
TestFile.java:9: error: unclosed string literal
        System.out.println("Error);
                           ^
2 errors
error: compilation failed
```

---

### C++

**Kiểm tra Cú pháp:**
```bash
g++ -c test.cpp
```

**Chạy Code:**
```bash
g++ -o test test.cpp && ./test
```

**Ví dụ Output (Lỗi):**
```
test.cpp:6:18: warning: missing terminating " character
std::cout << "Hello << std::endl;
                  ^
test.cpp:6:5: error: missing terminating " character
std::cout << "Hello << std::endl;
```

---

### C#

**Kiểm tra Cú pháp / Chạy:**
```bash
csc test.cs
```

---

### JavaScript

**Chạy Code:**
```bash
node test.js
```

---

## 📞 Tư vấn thêm

- **Lỗi kết nối Server**: Đảm bảo Server đã khởi động trước Client
- **Lỗi Judge0 API**: Kiểm tra API key và kết nối Internet
- **Lỗi Compilation**: Đảm bảo các compiler cần thiết đã được cài đặt (Java, Python, G++, v.v.)

---

**Version**: 1.0-SNAPSHOT  
**Author**: NqtPopCorn  
**Repository**: https://github.com/NqtPopCorn/LTM-codechecker

### Execute

```ps
g++ a.cpp -o main.exe
./main.exe
```

---

## JavaScript

**Prerequisite:** TypeScript compiler (`tsc`) must be installed (`npm install -g typescript`). Node.js is required for execution.

### Syntax Check

```ps
tsc test.js --allowJs --noEmit
```

**Example output (syntax error):**

```
test.js:1:29 - error TS1002: Unterminated string literal.

1 console.log("Hello, World!);

test.js:2:30 - error TS1002: Unterminated string literal.

2 ,console.log("Hello, World!);

Found 2 errors in the same file, starting at: test.js:1
```

### Execute

```ps
node test.js
```

---

## C#

**Prerequisite:** `csc` (Roslyn C# compiler, bundled with Visual Studio) must be on the system PATH.

> Setup: Add the Roslyn compiler directory to your environment PATH variable.
> Default path: `C:\Program Files\Microsoft Visual Studio\2022\Community\MSBuild\Current\Bin\Roslyn`

### Syntax Check / Compile

```ps
csc Program.cs
```

**Example output (error compile):**

```
Microsoft (R) Visual C# Compiler version 4.13.0-3.25167.3 (73eff2b5)
Copyright (C) Microsoft Corporation. All rights reserved.

Program.cs(7,27): error CS1010: Newline in constant
Program.cs(7,43): error CS1026: ) expected
Program.cs(7,43): error CS1002: ; expected
```

### Execute (after running csc)

```ps
Program.exe
```
