# Cấu trúc dự án

## Tổng quan

Đây là dự án Java Maven triển khai hệ thống **kiểm tra cú pháp theo mô hình client-server**. Server gọi các công cụ CLI (ví dụ: `pyright`, `g++`, `tsc`) dưới dạng tiến trình con để kiểm tra cú pháp và thực thi file mã nguồn. Client kết nối đến server qua socket và hiển thị kết quả trên giao diện Swing.

---

## Cây thư mục

```
dt7syntaxcheck/
├── pom.xml                         # Cấu hình build Maven
├── Readme.md                       # Tham chiếu lệnh CLI cho từng ngôn ngữ được hỗ trợ
├── STRUCTURE.md                    # File này
└── src/
    └── main/
        └── java/
            └── com/example/dt7syntaxcheck/
                ├── client/         # Phía client: giao diện người dùng và giao tiếp socket
                └── server/         # Phía server: interface, model và các cài đặt theo ngôn ngữ
                    └── python/     # Cài đặt cụ thể cho ngôn ngữ Python
```

---

## Package: `client`

> Đường dẫn: `src/main/java/com/example/dt7syntaxcheck/client/`

Xử lý phía người dùng của ứng dụng: giao diện Swing và kết nối socket đến server.

| File                   | Loại             | Chức năng                                                                                                                                                                                                     |
| ---------------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ClientUIFrame.java`   | Class (`JFrame`) | Cửa sổ Swing chính. Chứa vùng nhập code, các nút Check/Clear và panel hiển thị log lỗi. Khởi tạo `ClientService` khi chương trình chạy.                                                                       |
| `ClientUIFrame.form`   | NetBeans Form    | File thiết kế giao diện NetBeans cho `ClientUIFrame`. **Không chỉnh sửa thủ công.**                                                                                                                           |
| `ClientService.java`   | Class            | Quản lý kết nối TCP socket đến server. Tạo các luồng nền (background thread) để kết nối và nhận tin nhắn, tránh chặn luồng giao diện (EDT). Cung cấp các phương thức `connect()`, `send()` và `disconnect()`. |
| `MessageListener.java` | Interface        | Interface callback dùng bởi `ClientService` để thông báo cho giao diện khi nhận tin nhắn (`onMessageReceived`) hoặc khi mất kết nối (`onDisconnected`).                                                       |

### Luồng hoạt động (Client)

```
ClientUIFrame
    └── tạo ClientService(MessageListener)
            └── connect(host, port)  →  tạo Thread → Socket
                    └── startReceiving()  →  tạo Thread → đọc từng dòng
                            └── gọi listener.onMessageReceived(msg)
                                    └── cập nhật pnlErrorLog (trên EDT)
```

---

## Package: `server`

> Đường dẫn: `src/main/java/com/example/dt7syntaxcheck/server/`

Chứa điểm khởi đầu của server, model dữ liệu dùng chung và các interface độc lập với ngôn ngữ mà mọi cài đặt ngôn ngữ cụ thể phải tuân theo.

| File                   | Loại           | Chức năng                                                                                                                                                                  |
| ---------------------- | -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ServerMain.java`      | Class (`main`) | Điểm khởi đầu của server. Hiện đang kết nối `PythonSyntaxChecker` và `PythonExecutor` để kiểm thử. Chứa vòng lặp socket server đã được comment lại (bộ khung echo server). |
| `ErrorLog.java`        | Model Class    | Class dữ liệu đại diện cho một lỗi cú pháp. Các trường: `line` (int), `index` (int), `message` (String).                                                                   |
| `ISyntaxChecker.java`  | Interface      | Hợp đồng cho các bộ kiểm tra cú pháp. Phương thức: `ErrorLog[] syntaxCheck(String filePath)`.                                                                              |
| `ICodeExecutor.java`   | Interface      | Hợp đồng cho các bộ thực thi code. Phương thức: `String execute(String filePath)`.                                                                                         |
| `IErrorLogParser.java` | Interface      | Hợp đồng để phân tích từng dòng output CLI thô thành đối tượng `ErrorLog`. Phương thức: `ErrorLog parse(String text)`.                                                     |
| `IFormatter.java`      | Interface      | Interface giữ chỗ cho tính năng định dạng output trong tương lai. Hiện tại để trống.                                                                                       |

---

## Package: `server.python`

> Đường dẫn: `src/main/java/com/example/dt7syntaxcheck/server/python/`

Cài đặt cụ thể các interface của server cho ngôn ngữ **Python**. Sử dụng `ProcessBuilder` để gọi các công cụ CLI dưới dạng tiến trình con.

| File                        | Implements        | Chức năng                                                                                                                                                       |
| --------------------------- | ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `PythonSyntaxChecker.java`  | `ISyntaxChecker`  | Chạy lệnh `python -m pyright <filePath>` và thu thập các dòng lỗi. Uỷ quyền từng dòng cho `PythonErrorLogParser` để tạo ra `ErrorLog[]`.                        |
| `PythonExecutor.java`       | `ICodeExecutor`   | Chạy lệnh `python <filePath>` và bắt toàn bộ stdout/stderr. Trả về kết quả dưới dạng `String`.                                                                  |
| `PythonErrorLogParser.java` | `IErrorLogParser` | Phân tích một dòng output của pyright bằng regex `.*:(\d+):(\d+)\s+-\s+error:\s+(.+)` và ánh xạ sang `ErrorLog`. Trả về `null` nếu dòng đó không phải dòng lỗi. |

### Sơ đồ phụ thuộc (server.python)

```
PythonSyntaxChecker  ──implements──▶  ISyntaxChecker
    └── dùng  PythonErrorLogParser  ──implements──▶  IErrorLogParser
                    └── trả về  ErrorLog

PythonExecutor  ──implements──▶  ICodeExecutor
```

---

## Thêm ngôn ngữ mới

Để thêm hỗ trợ cho một ngôn ngữ mới (ví dụ: C++), làm theo các bước sau:

1. Tạo sub-package mới trong `server/` (ví dụ: `server/cpp/`).
2. Cài đặt `ISyntaxChecker` — gọi CLI của trình biên dịch (ví dụ: `g++ -c`) thông qua `ProcessBuilder`.
3. Cài đặt `IErrorLogParser` — viết regex để phân tích định dạng lỗi của trình biên dịch đó thành `ErrorLog`.
4. Cài đặt `ICodeExecutor` — gọi file thực thi đã biên dịch hoặc trình thông dịch.
5. Kết nối checker/executor mới vào `ServerMain`.
