@echo off
setlocal
cd /d "%~dp0"

echo [0/4] Stopping previous app process (if any)...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -like '*canteen-mvp-0.0.1-SNAPSHOT.jar*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"

echo [1/5] Starting MySQL (docker compose)...
cd /d ".."
docker compose up -d mysql
if errorlevel 1 (
  echo Failed to start MySQL with Docker.
  echo Please start Docker Desktop first, then rerun this script.
  exit /b 1
)

cd /d "backend"

echo [2/5] Waiting for MySQL to be ready...
set /a MAX_RETRY=60
set /a RETRY=0
:wait_loop
docker exec canteen-mysql mysqladmin ping -uroot -proot123 --silent >nul 2>nul
if not errorlevel 1 goto db_ready
set /a RETRY+=1
if %RETRY% GEQ %MAX_RETRY% (
  echo MySQL is not ready after %MAX_RETRY% seconds.
  echo Please check: docker logs canteen-mysql
  exit /b 1
)
timeout /t 1 /nobreak >nul
goto wait_loop
:db_ready

echo [3/5] Building project...
call mvnw.cmd -q clean package -DskipTests
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)

echo [4/5] Checking port 8080...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr LISTENING ^| findstr :8080') do (
  if not "%%p"=="" (
    echo Port 8080 is already in use by PID %%p.
    echo Please stop that process and rerun this script.
    exit /b 1
  )
)

echo [5/5] Starting application...
java -jar target\canteen-mvp-0.0.1-SNAPSHOT.jar
