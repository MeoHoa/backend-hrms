@echo off
REM Script để build project và copy JAR vào thư mục service

echo ========================================
echo HRMS Backend Build and Deploy Script
echo ========================================
echo.

set SERVICE_DIR=C:\HRMS-Service
set JAR_SOURCE=target\hrms-backend-0.0.1-SNAPSHOT.jar
set JAR_DEST=%SERVICE_DIR%\hrms-backend-0.0.1-SNAPSHOT.jar

echo [1/3] Dang build project...
call mvn clean package -DskipTests
if %errorLevel% neq 0 (
    echo ERROR: Build that bai!
    pause
    exit /b 1
)
echo OK: Build thanh cong!

echo.
echo [2/3] Kiem tra JAR file...
if not exist "%JAR_SOURCE%" (
    echo ERROR: Khong tim thay JAR file sau khi build!
    pause
    exit /b 1
)
echo OK: JAR file da duoc tao

echo.
echo [3/3] Copy JAR vao thu muc service...
if not exist "%SERVICE_DIR%" (
    echo Tao thu muc service...
    mkdir "%SERVICE_DIR%"
    mkdir "%SERVICE_DIR%\logs"
)

copy /Y "%JAR_SOURCE%" "%JAR_DEST%"
if %errorLevel% neq 0 (
    echo ERROR: Copy JAR that bai!
    pause
    exit /b 1
)
echo OK: JAR da duoc copy vao %SERVICE_DIR%

echo.
echo ========================================
echo Hoan tat!
echo ========================================
echo.
echo JAR file da duoc copy vao: %JAR_DEST%
echo.
echo De cap nhat service:
echo   1. Dung service: nssm stop HRMS-Backend
echo   2. Khoi dong lai: nssm start HRMS-Backend
echo.
pause

