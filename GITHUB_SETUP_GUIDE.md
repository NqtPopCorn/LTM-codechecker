# 🔐 GitHub Setup Guide - Cấu hình GitHub Token & Gist

## ❌ Vấn đề Hiện Tại

```
[-] Lỗi đăng ký server (Code: 401)
[-] Response: {
  "message": "Bad credentials",
  "documentation_url": "https://docs.github.com/rest",
  "status": "401"
}
```

**Nguyên nhân:** GitHub Token đã hết hạn hoặc không hợp lệ

---

## ✅ Giải Pháp - 3 Bước

### **Bước 1️⃣: Tạo GitHub Personal Access Token**

1. Đi tới: https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Điền thông tin:
   - **Token name:** `LTM-CodeChecker` (hoặc tên khác)
   - **Expiration:** `90 days` (hoặc tùy chọn)
4. **Chọn scopes (quyền):**
   - ☑️ **`gist`** (cho phép tạo, đọc, cập nhật gists)
5. Click **"Generate token"**
6. ⚠️ **COPY TOKEN NGAY LẬP TỨC** (chỉ hiển thị lần này!)
   - Ví dụ: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

---

### **Bước 2️⃣: Tạo GitHub Gist**

1. Đi tới: https://gist.github.com
2. Click **"Create a new gist"**
3. **Filename:** `server_registry.txt`
4. **Paste nội dung (tạm thời):**
   ```
   127.0.0.1:5000
   ```
5. **Description:** `Server Registry for LTM-CodeChecker`
6. **Visibility:** `Public` (hoặc Secret nếu bạn muốn private)
7. Click **"Create public/secret gist"**

8. ✅ **Lấy Gist ID từ URL:**
   - URL: `https://gist.github.com/YOUR_USERNAME/814c622f74340fe2f5e0b92cf385f95b`
   - **Gist ID:** `814c622f74340fe2f5e0b92cf385f95b` (phần cuối)

---

### **Bước 3️⃣: Set Environment Variables**

#### **Cách 1: Windows - Command Prompt (cmd.exe)**

```cmd
set GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
set GITHUB_GIST_ID=814c622f74340fe2f5e0b92cf385f95b

java -cp target/classes com.example.dt7syntaxcheck.server.ServerMain
```

#### **Cách 2: Windows - PowerShell**

```powershell
$env:GITHUB_TOKEN='ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
$env:GITHUB_GIST_ID='814c622f74340fe2f5e0b92cf385f95b'

java -cp target/classes com.example.dt7syntaxcheck.server.ServerMain
```

#### **Cách 3: Windows - Cấu hình Vĩnh Viễn (System Environment Variables)**

1. **Mở Command Prompt as Administrator**
2. Chạy lệnh:
   ```cmd
   setx GITHUB_TOKEN "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
   setx GITHUB_GIST_ID "814c622f74340fe2f5e0b92cf385f95b"
   ```
3. **Đóng Terminal và mở lại** (để environment variables có hiệu lực)
4. Verify:
   ```cmd
   echo %GITHUB_TOKEN%
   echo %GITHUB_GIST_ID%
   ```

#### **Cách 4: Linux/Mac**

```bash
export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
export GITHUB_GIST_ID="814c622f74340fe2f5e0b92cf385f95b"

java -cp target/classes com.example.dt7syntaxcheck.server.ServerMain
```

---

## 🧪 Test & Verify

### **1. Kiểm tra Environment Variables được set**

**Command Prompt:**
```cmd
echo %GITHUB_TOKEN%
echo %GITHUB_GIST_ID%
```

**PowerShell:**
```powershell
echo $env:GITHUB_TOKEN
echo $env:GITHUB_GIST_ID
```

---

### **2. Run Server và kiểm tra output**

```bash
cd d:\LTM-codechecker
mvn clean compile
java -cp target/classes com.example.dt7syntaxcheck.server.ServerMain
```

✅ **Output thành công sẽ như thế này:**
```
=================================================
   SERVER KIỂM TRA VÀ THỰC THI CODE   
=================================================
Đang khởi động hệ thống...
[INFO] Mode: Per-Client Key Pair (ENABLED)
[INFO] Mỗi client connection → sinh unique RSA key pair
[INFO] ✓ Key isolation, ✓ Better security

[INFO] Server đang lắng nghe kết nối tại port 5000...

[INFO] Đang đăng ký server IP lên GitHub Gist...
[INFO] Đang đăng ký server IP: 192.168.1.100:5000
[+] Đăng ký server thành công!
[+] Server IP đã được lưu tại GitHub Gist: 192.168.1.100:5000

[INFO] Server đang lắng nghe kết nối...
```

❌ **Nếu vẫn thấy lỗi 401:**
- Kiểm tra lại GitHub token (hết hạn?)
- Kiểm tra token có đủ quyền `gist`
- Kiểm tra Gist ID đúng

---

## 🛡️ Bảo Mật - Các Lưu Ý Quan Trọng

### ⚠️ **KHÔNG**:
- ❌ Commit token vào Git repository
- ❌ Share token công khai
- ❌ Để token trong code source
- ❌ Push file `.env` chứa token

### ✅ **NÊN**:
- ✅ Sử dụng environment variables
- ✅ Sử dụng `.env` file (gitignore nó)
- ✅ Sử dụng GitHub Secrets (nếu dùng CI/CD)
- ✅ Rotate token định kỳ
- ✅ Set expiration time cho token

---

## 📝 Ví Dụ Đầy Đủ - Setup từ A-Z

### **Terminal: Windows PowerShell**

```powershell
# Bước 1: Tạo token trên GitHub (manual) - Copy token

# Bước 2: Tạo Gist trên GitHub (manual) - Copy Gist ID

# Bước 3: Set environment variables
$env:GITHUB_TOKEN='ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
$env:GITHUB_GIST_ID='814c622f74340fe2f5e0b92cf385f95b'

# Bước 4: Verify
echo "Token: $env:GITHUB_TOKEN"
echo "Gist ID: $env:GITHUB_GIST_ID"

# Bước 5: Build & Run
cd d:\LTM-codechecker
mvn clean compile
java -cp target/classes com.example.dt7syntaxcheck.server.ServerMain

# ✅ Nếu thấy "[+] Đăng ký server thành công!" → Setup hoàn tất!
```

---

## 🔧 Troubleshooting

### **Lỗi: "Bad credentials (401)"**
- **Nguyên nhân:** Token sai hoặc hết hạn
- **Giải pháp:** 
  1. Tạo token mới từ GitHub
  2. Check expiration date
  3. Set environment variable lại

### **Lỗi: "Not Found (404)"**
- **Nguyên nhân:** Gist ID sai
- **Giải pháp:** Kiểm tra lại Gist URL trên GitHub

### **Lỗi: "Insufficient permissions"**
- **Nguyên nhân:** Token không có quyền `gist`
- **Giải pháp:** Tạo token mới với quyền đầy đủ

### **Environment variable không có hiệu lực**
- **Cách 1:** Đóng terminal & mở lại
- **Cách 2:** Reboot máy
- **Cách 3:** Dùng `setx` thay vì `set` (để save vĩnh viễn)

---

## 📚 Code Changes Made

File `ServiceRegistry.java` đã được cập nhật:

✅ **Before (Không an toàn):**
```java
private static final String GITHUB_TOKEN = "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";  // ❌ Hardcoded
private static final String GIST_ID = "814c622f74340fe2f5e0b92cf385f95b";
```

✅ **After (An toàn):**
```java
private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN") != null 
    ? System.getenv("GITHUB_TOKEN") 
    : "";  // ✅ Đọc từ environment variable

private static final String GIST_ID = System.getenv("GITHUB_GIST_ID") != null 
    ? System.getenv("GITHUB_GIST_ID") 
    : "814c622f74340fe2f5e0b92cf385f95b";  // ✅ Có default
```

✅ **Error Messages được cải thiện:**
- Chi tiết cách tạo token
- Chi tiết cách tạo Gist
- Chi tiết cách set environment variables

---

## 📋 Checklist Cuối Cùng

- [ ] Tạo GitHub Personal Access Token (quyền: gist)
- [ ] Tạo Gist mới trên GitHub
- [ ] Copy Token → Set `GITHUB_TOKEN` environment variable
- [ ] Copy Gist ID → Set `GITHUB_GIST_ID` environment variable
- [ ] Verify environment variables (`echo %GITHUB_TOKEN%`)
- [ ] Build: `mvn clean compile`
- [ ] Run Server: `java -cp target/classes com.example.dt7syntaxcheck.server.ServerMain`
- [ ] Check output: "[+] Đăng ký server thành công!"
- [ ] ✅ Done! Server đã register thành công

---

## 📞 Nếu vẫn có vấn đề

1. **Kiểm tra lại toàn bộ các bước trên**
2. **Verify token** tại: https://api.github.com/user (paste URL trên browser, đăng nhập GitHub)
3. **Kiểm tra Gist permissions** - Gist có public không? Có file `server_registry.txt` không?
4. **Xem GitHub API documentation:** https://docs.github.com/rest/gists

