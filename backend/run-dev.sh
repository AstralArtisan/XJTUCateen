#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

echo "[0/5] Stopping previous app process (if any)..."
pkill -f "canteen-mvp-0.0.1-SNAPSHOT.jar" 2>/dev/null || true

echo "[1/5] Starting MySQL (docker compose)..."
cd ..
docker compose up -d
cd backend

echo "[2/5] Waiting for MySQL to be ready..."
for i in $(seq 1 60); do
  if docker exec canteen-mysql mysqladmin ping -uroot -proot123 --silent >/dev/null 2>&1; then
    break
  fi
  if [ "$i" -eq 60 ]; then
    echo "MySQL is not ready after 60 seconds."
    echo "Please check: docker logs canteen-mysql"
    exit 1
  fi
  sleep 1
done

echo "[3/5] Building project..."
./mvnw -q clean package -DskipTests

echo "[4/5] Checking port 8080..."
if lsof -i :8080 >/dev/null 2>&1; then
  echo "Port 8080 is already in use. Please stop that process and rerun."
  exit 1
fi

echo "[5/5] Starting application..."
java -jar target/canteen-mvp-0.0.1-SNAPSHOT.jar
