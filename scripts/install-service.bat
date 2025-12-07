@echo off
REM Script để cài đặt HRMS Backend như Windows Service sử dụng NSSM
REM Chạy script này với quyền Administrator

echo ========================================
echo HRMS Backend Service Installer
echo ========================================
echo.

REM Kiểm tra quyền Administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ERROR: Script phai chay voi quyen Administrator!
    echo Click chuot phai va chon "Run as administrator"
    pause
    exit /b 1
)

REM Biến cấu hình
set SERVICE_NAME=HRMS-Backend
set SERVICE_DIR=C:\HRMS-Service
set JAR_FILE=%SERVICE_DIR%\hrms-backend-0.0.1-SNAPSHOT.jar
set JAVA_HOME=C:\Program Files\Java\jdk-17
set NSSM_DIR=C:\Program Files\nssm\win64

echo [1/5] Kiem tra Java...
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Khong tim thay Java tai %JAVA_HOME%
    echo Vui long cai dat Java JDK 17 va cap nhat JAVA_HOME trong script nay
    pause
    exit /b 1
)
echo OK: Java da tim thay

echo.
echo [2/5] Kiem tra NSSM...
if not exist "%NSSM_DIR%\nssm.exe" (
    echo ERROR: Khong tim thay NSSM tai %NSSM_DIR%
    echo Vui long cai dat NSSM va cap nhat NSSM_DIR trong script nay
    echo Download: https://nssm.cc/download
    pause
    exit /b 1
)
echo OK: NSSM da tim thay

echo.
echo [3/5] Kiem tra JAR file...
if not exist "%JAR_FILE%" (
    echo ERROR: Khong tim thay JAR file tai %JAR_FILE%
    echo Vui long build project truoc: mvn clean package
    echo Sau do copy JAR file vao %SERVICE_DIR%
    pause
    exit /b 1
)
echo OK: JAR file da tim thay

echo.
echo [4/5] Tao thu muc service...
if not exist "%SERVICE_DIR%" mkdir "%SERVICE_DIR%"
if not exist "%SERVICE_DIR%\logs" mkdir "%SERVICE_DIR%\logs"
echo OK: Thu muc da tao

echo.
echo [5/5] Cai dat Service...
cd /d "%NSSM_DIR%"

REM Xóa service cũ nếu tồn tại
nssm stop "%SERVICE_NAME%" >nul 2>&1
nssm remove "%SERVICE_NAME%" confirm >nul 2>&1

REM Cài đặt service mới
echo Dang cai dat service...
nssm install "%SERVICE_NAME%" "%JAVA_HOME%\bin\java.exe" "-jar %JAR_FILE%"

REM Cấu hình service
echo Dang cau hinh service...
nssm set "%SERVICE_NAME%" AppDirectory "%SERVICE_DIR%"
nssm set "%SERVICE_NAME%" Description "HRMS Backend Application Service"
nssm set "%SERVICE_NAME%" AppStdout "%SERVICE_DIR%\logs\output.log"
nssm set "%SERVICE_NAME%" AppStderr "%SERVICE_DIR%\logs\error.log"
nssm set "%SERVICE_NAME%" AppRestartDelay 5000
nssm set "%SERVICE_NAME%" AppExit Default Restart
nssm set "%SERVICE_NAME%" Start SERVICE_AUTO_START

echo.
echo ========================================
echo Cai dat thanh cong!
echo ========================================
echo.
echo De khoi dong service:
echo   nssm start %SERVICE_NAME%
echo.
echo Hoac mo Services (Win+R, go: services.msc)
echo Tim service "%SERVICE_NAME%" va Start
echo.
pause

