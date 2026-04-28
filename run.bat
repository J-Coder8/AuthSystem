@echo off
setlocal
echo Starting Authentication System 2.0...

for /f "tokens=5" %%P in ('powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty OwningProcess"') do (
    if not "%%P"=="" (
        echo Stopping existing backend on port 8082 (PID %%P^)...
        powershell -NoProfile -Command "Stop-Process -Id %%P -Force -ErrorAction SilentlyContinue"
        timeout /t 2 /nobreak >nul
    )
)

if exist "backend\mail.env" (
    echo Loading Gmail settings from backend\mail.env...
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("backend\mail.env") do (
        if not "%%~A"=="" set "%%~A=%%~B"
    )
) else (
    echo Gmail settings not found in backend\mail.env.
    echo OTP email delivery will require AUTH_GMAIL_USER and AUTH_GMAIL_APP_PASSWORD.
)

echo Starting Backend...
start "Backend" cmd /k "cd backend && mvn spring-boot:run"

echo Waiting for backend...
timeout /t 10 /nobreak >nul

echo Starting Frontend...
start "Frontend" cmd /k "cd frontend && mvn javafx:run"

echo All services started!
echo Backend: http://localhost:8082
echo H2 Console: http://localhost:8082/h2-console (jdbc:h2:file:./data/authsystem)
