# 📘 HƯỚNG DẪN SETUP PROJECT HRMS BACKEND

Hướng dẫn chi tiết từ A-Z để setup và chạy project HRMS Backend cho người mới bắt đầu.

---

## 📋 MỤC LỤC

1. [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống)
2. [Cài đặt môi trường](#2-cài-đặt-môi-trường)
3. [Clone project](#3-clone-project)
4. [Cấu hình Database](#4-cấu-hình-database)
5. [Cấu hình Application](#5-cấu-hình-application)
6. [Build và chạy project](#6-build-và-chạy-project)
7. [Setup Windows Service](#7-setup-windows-service)
8. [Kiểm tra và Test](#8-kiểm-tra-và-test)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. YÊU CẦU HỆ THỐNG

### Phần mềm cần cài đặt:

- **Java JDK 17** (hoặc cao hơn)
- **Apache Maven 3.6+**
- **MySQL 8.0+**
- **Git** (để clone project)
- **IDE** (IntelliJ IDEA, Eclipse, hoặc VS Code - tùy chọn)

### Hệ điều hành:

- Windows 10/11 (64-bit)
- Hoặc Windows Server 2016+

---

## 2. CÀI ĐẶT MÔI TRƯỜNG

### 2.1. Cài đặt Java JDK 17

1. **Tải Java JDK 17:**
   - Truy cập: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
   - Chọn: **Windows x64 Installer** (jdk-17_windows-x64_bin.exe)

2. **Cài đặt:**
   - Chạy file `.exe` vừa tải
   - Nhấn Next → Next → Install
   - Đợi cài đặt hoàn tất

3. **Cấu hình biến môi trường:**
   - Mở **System Properties** → **Environment Variables**
   - Thêm biến mới:
     - **JAVA_HOME**: `C:\Program Files\Java\jdk-17` (đường dẫn thực tế của bạn)
   - Thêm vào **Path**:
     - `%JAVA_HOME%\bin`

4. **Kiểm tra:**
   ```bash
   java -version
   javac -version
   ```
   Kết quả phải hiển thị version 17.x.x

### 2.2. Cài đặt Apache Maven

1. **Tải Maven:**
   - Truy cập: https://maven.apache.org/download.cgi
   - Tải file: **apache-maven-3.9.x-bin.zip**

2. **Giải nén:**
   - Giải nén vào: `C:\Program Files\Apache\maven` (hoặc thư mục bạn muốn)

3. **Cấu hình biến môi trường:**
   - Thêm biến mới:
     - **MAVEN_HOME**: `C:\Program Files\Apache\maven`
   - Thêm vào **Path**:
     - `%MAVEN_HOME%\bin`

4. **Kiểm tra:**
   ```bash
   mvn -version
   ```
   Kết quả phải hiển thị version Maven và Java

### 2.3. Cài đặt MySQL

1. **Tải MySQL:**
   - Truy cập: https://dev.mysql.com/downloads/installer/
   - Chọn: **MySQL Installer for Windows**

2. **Cài đặt:**
   - Chọn **Developer Default** hoặc **Server only**
   - Thiết lập root password (nhớ password này để dùng sau)
   - Port mặc định: **3306**
   - Đợi cài đặt hoàn tất

3. **Kiểm tra:**
   - Mở **MySQL Command Line Client** hoặc **MySQL Workbench**
   - Đăng nhập bằng root password

### 2.4. Cài đặt Git (nếu chưa có)

1. **Tải Git:**
   - Truy cập: https://git-scm.com/download/win
   - Tải và cài đặt

2. **Kiểm tra:**
   ```bash
   git --version
   ```

---

## 3. CLONE PROJECT

### 3.1. Mở Command Prompt hoặc PowerShell

### 3.2. Clone repository

```bash
# Di chuyển đến thư mục bạn muốn lưu project
cd C:\Projects

# Clone project (thay URL bằng URL thực tế của repository)
git clone <URL_REPOSITORY>
cd hrms-backend
```

**Ví dụ:**
```bash
git clone https://github.com/your-org/hrms-backend.git
cd hrms-backend
```

---

## 4. CẤU HÌNH DATABASE

### 4.1. Tạo database

1. **Mở MySQL Command Line Client** hoặc **MySQL Workbench**

2. **Tạo database:**
   ```sql
   CREATE DATABASE HRMS CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Kiểm tra:**
   ```sql
   SHOW DATABASES;
   ```
   Phải thấy database `HRMS` trong danh sách

### 4.2. Tạo user và cấp quyền (tùy chọn)

```sql
CREATE USER 'hrms_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON HRMS.* TO 'hrms_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 5. CẤU HÌNH APPLICATION

### 5.1. Mở file cấu hình

Mở file: `src/main/resources/application.yml`

### 5.2. Cập nhật thông tin database

Tìm và sửa các thông tin sau:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/HRMS?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root          # Thay bằng username MySQL của bạn
    password: your_password # Thay bằng password MySQL của bạn
```

**Lưu ý:**
- Nếu dùng user khác root, thay `root` bằng username của bạn
- Thay `your_password` bằng password MySQL thực tế

### 5.3. Cấu hình Email (tùy chọn)

Nếu muốn dùng tính năng gửi email, cập nhật:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password  # App password từ Gmail
```

**Lưu ý:** Nếu dùng Gmail, cần tạo **App Password** (không dùng password thường)

### 5.4. Cấu hình Port (nếu cần)

Mặc định server chạy trên port **8080**. Nếu muốn đổi:

```yaml
server:
  port: 8080  # Đổi số port bạn muốn
```

---

## 6. BUILD VÀ CHẠY PROJECT

### 6.1. Build project

Mở **Command Prompt** hoặc **PowerShell** trong thư mục project:

```bash
# Di chuyển vào thư mục project
cd C:\Projects\hrms-backend

# Build project (tải dependencies và compile)
mvn clean install
```

**Lần đầu tiên có thể mất 5-10 phút** để tải các thư viện (dependencies).

### 6.2. Chạy project

#### Cách 1: Chạy bằng Maven

```bash
mvn spring-boot:run
```

#### Cách 2: Chạy bằng JAR file

```bash
# Build JAR file
mvn clean package

# Chạy JAR
java -jar target/hrms-backend-0.0.1-SNAPSHOT.jar
```

### 6.3. Kiểm tra server đã chạy

Mở trình duyệt và truy cập:
- **Health check:** http://localhost:8080/actuator/health (nếu có)
- **API Base:** http://localhost:8080/api

Nếu thấy response (có thể là lỗi 401/403), nghĩa là server đã chạy thành công!

---

## 7. SETUP WINDOWS SERVICE

Để chạy ứng dụng như một Windows Service (tự động khởi động khi Windows boot), có 2 cách:

### Cách 1: Sử dụng NSSM (NON-SUCKING SERVICE MANAGER) - Khuyên dùng

#### Bước 1: Tải NSSM

1. Truy cập: https://nssm.cc/download
2. Tải **nssm-2.24.zip** (hoặc version mới nhất)
3. Giải nén vào: `C:\Program Files\nssm` (hoặc thư mục bạn muốn)

#### Bước 2: Build JAR file

```bash
cd C:\Projects\hrms-backend
mvn clean package
```

JAR file sẽ được tạo tại: `target\hrms-backend-0.0.1-SNAPSHOT.jar`

#### Bước 3: Tạo thư mục cho service

```bash
# Tạo thư mục
mkdir C:\HRMS-Service
mkdir C:\HRMS-Service\logs

# Copy JAR file vào thư mục service
copy target\hrms-backend-0.0.1-SNAPSHOT.jar C:\HRMS-Service\
```

#### Bước 4: Cài đặt Service

1. **Mở Command Prompt với quyền Administrator**

2. **Chạy lệnh cài đặt:**

```bash
cd "C:\Program Files\nssm\win64"

nssm install HRMS-Backend "C:\Program Files\Java\jdk-17\bin\java.exe" "-jar C:\HRMS-Service\hrms-backend-0.0.1-SNAPSHOT.jar"
```

3. **Cấu hình Service:**

```bash
# Đặt thư mục làm việc
nssm set HRMS-Backend AppDirectory "C:\HRMS-Service"

# Đặt mô tả
nssm set HRMS-Backend Description "HRMS Backend Application Service"

# Cấu hình log
nssm set HRMS-Backend AppStdout "C:\HRMS-Service\logs\output.log"
nssm set HRMS-Backend AppStderr "C:\HRMS-Service\logs\error.log"

# Tự động khởi động lại khi crash
nssm set HRMS-Backend AppRestartDelay 5000
nssm set HRMS-Backend AppExit Default Restart

# Khởi động cùng Windows
nssm set HRMS-Backend Start SERVICE_AUTO_START
```

#### Bước 5: Khởi động Service

```bash
# Khởi động service
nssm start HRMS-Backend

# Kiểm tra trạng thái
nssm status HRMS-Backend
```

#### Bước 6: Quản lý Service

```bash
# Dừng service
nssm stop HRMS-Backend

# Khởi động lại
nssm restart HRMS-Backend

# Xóa service (nếu cần)
nssm remove HRMS-Backend confirm
```

**Hoặc sử dụng Services Manager:**
- Mở **Services** (Win + R → `services.msc`)
- Tìm service **HRMS-Backend**
- Click chuột phải để Start/Stop/Restart

### Cách 2: Sử dụng WinSW (Windows Service Wrapper)

#### Bước 1: Tải WinSW

1. Truy cập: https://github.com/winsw/winsw/releases
2. Tải: **WinSW-x64.exe**

#### Bước 2: Tạo file cấu hình

Tạo file `HRMS-Backend.xml` trong thư mục `C:\HRMS-Service`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<service>
  <id>HRMS-Backend</id>
  <name>HRMS Backend Service</name>
  <description>HRMS Backend Application Service</description>
  <executable>java</executable>
  <arguments>-jar "C:\HRMS-Service\hrms-backend-0.0.1-SNAPSHOT.jar"</arguments>
  <workingdirectory>C:\HRMS-Service</workingdirectory>
  <logmode>rotate</logmode>
  <logpath>C:\HRMS-Service\logs</logpath>
  <startmode>Automatic</startmode>
  <onfailure action="restart" delay="5 sec"/>
  <onfailure action="restart" delay="10 sec"/>
  <onfailure action="reboot" delay="15 sec"/>
</service>
```

#### Bước 3: Cài đặt Service

```bash
# Copy WinSW vào thư mục service
copy WinSW-x64.exe C:\HRMS-Service\HRMS-Backend.exe

# Cài đặt
cd C:\HRMS-Service
HRMS-Backend.exe install
HRMS-Backend.exe start
```

---

## 8. KIỂM TRA VÀ TEST

### 8.1. Kiểm tra Service đang chạy

1. Mở **Services** (Win + R → `services.msc`)
2. Tìm **HRMS-Backend**
3. Kiểm tra Status phải là **Running**

### 8.2. Kiểm tra Log

```bash
# Xem log output
type C:\HRMS-Service\logs\output.log

# Xem log error
type C:\HRMS-Service\logs\error.log
```

### 8.3. Test API

Mở trình duyệt hoặc dùng **Postman**:

```bash
# Test health check (nếu có)
GET http://localhost:8080/actuator/health

# Test API login (ví dụ)
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

### 8.4. Kiểm tra Database

Mở **MySQL Workbench** hoặc **Command Line**:

```sql
USE HRMS;
SHOW TABLES;
```

Phải thấy các bảng đã được tạo tự động (nếu `ddl-auto: update`).

---

## 9. TROUBLESHOOTING

### Lỗi: "Java not found"

**Nguyên nhân:** Java chưa được cài đặt hoặc chưa cấu hình biến môi trường.

**Giải pháp:**
1. Kiểm tra Java đã cài: `java -version`
2. Nếu chưa có, cài đặt Java JDK 17
3. Cấu hình lại biến môi trường `JAVA_HOME` và `Path`

### Lỗi: "Maven not found"

**Nguyên nhân:** Maven chưa được cài đặt hoặc chưa cấu hình biến môi trường.

**Giải pháp:**
1. Kiểm tra Maven: `mvn -version`
2. Cài đặt Maven và cấu hình `MAVEN_HOME`

### Lỗi: "Cannot connect to MySQL"

**Nguyên nhân:** 
- MySQL chưa chạy
- Sai username/password
- Sai port hoặc database name

**Giải pháp:**
1. Kiểm tra MySQL đang chạy: Mở **Services** → tìm **MySQL**
2. Kiểm tra lại thông tin trong `application.yml`
3. Test kết nối bằng MySQL Workbench

### Lỗi: "Port 8080 already in use"

**Nguyên nhân:** Port 8080 đã được sử dụng bởi ứng dụng khác.

**Giải pháp:**
1. Tìm process đang dùng port 8080:
   ```bash
   netstat -ano | findstr :8080
   ```
2. Kill process đó hoặc đổi port trong `application.yml`

### Lỗi: "Service failed to start"

**Nguyên nhân:** 
- JAR file không tồn tại
- Java path sai
- Thiếu quyền Administrator

**Giải pháp:**
1. Kiểm tra JAR file có tồn tại không
2. Kiểm tra Java path trong service config
3. Chạy Command Prompt với quyền Administrator
4. Xem log error: `C:\HRMS-Service\logs\error.log`

### Lỗi: "Table doesn't exist"

**Nguyên nhân:** Database chưa được tạo hoặc migration chưa chạy.

**Giải pháp:**
1. Tạo database: `CREATE DATABASE HRMS;`
2. Kiểm tra `ddl-auto` trong `application.yml` (nên dùng `update` cho lần đầu)
3. Restart application để tạo bảng tự động

### Lỗi khi build: "Dependencies download failed"

**Nguyên nhân:** Mất kết nối internet hoặc Maven repository không truy cập được.

**Giải pháp:**
1. Kiểm tra kết nối internet
2. Thử build lại: `mvn clean install -U`
3. Nếu ở Việt Nam, có thể cần cấu hình Maven mirror

---

## 📝 LƯU Ý QUAN TRỌNG

1. **Bảo mật:**
   - Không commit file `application.yml` có thông tin password thật lên Git
   - Sử dụng biến môi trường hoặc file config riêng cho production

2. **Backup:**
   - Backup database thường xuyên
   - Backup file JAR và config trước khi update

3. **Update:**
   - Khi có code mới, pull về và rebuild:
     ```bash
     git pull
     mvn clean package
     # Copy JAR mới vào C:\HRMS-Service
     # Restart service
     ```

4. **Monitoring:**
   - Kiểm tra log thường xuyên
   - Monitor memory và CPU usage
   - Setup alert nếu service down

---

## 🆘 HỖ TRỢ

Nếu gặp vấn đề không giải quyết được:

1. Kiểm tra log: `C:\HRMS-Service\logs\`
2. Kiểm tra Windows Event Viewer
3. Liên hệ team lead hoặc developer

---

## ✅ CHECKLIST SETUP

- [ ] Java JDK 17 đã cài và cấu hình
- [ ] Maven đã cài và cấu hình
- [ ] MySQL đã cài và chạy
- [ ] Database HRMS đã tạo
- [ ] File `application.yml` đã cấu hình đúng
- [ ] Project đã build thành công (`mvn clean package`)
- [ ] JAR file đã được tạo
- [ ] Service đã được cài đặt
- [ ] Service đang chạy (Status = Running)
- [ ] API đã test thành công

---

**Chúc bạn setup thành công! 🎉**

