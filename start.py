"""Start the Java backend and Vue frontend together.

Usage:
    python start.py
"""
from __future__ import annotations

import os
import signal
import shutil
import socket
import subprocess
import sys
import time
import webbrowser
from pathlib import Path


ROOT = Path(__file__).resolve().parent
BACKEND_HOST = "127.0.0.1"
BACKEND_PORT = 8000
FRONTEND_HOST = "127.0.0.1"
FRONTEND_PORT = 5173
BACKEND_URL = f"http://{BACKEND_HOST}:{BACKEND_PORT}"
FRONTEND_URL = f"http://{FRONTEND_HOST}:{FRONTEND_PORT}"


def command_path(name: str) -> str:
    if os.name == "nt":
        return shutil.which(f"{name}.cmd") or shutil.which(name) or name
    return shutil.which(name) or name


def is_port_open(host: str, port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.settimeout(0.4)
        return sock.connect_ex((host, port)) == 0


def wait_for_port(host: str, port: int, timeout: float) -> bool:
    deadline = time.time() + timeout
    while time.time() < deadline:
        if is_port_open(host, port):
            return True
        time.sleep(0.25)
    return False


def start_process(name: str, command: list[str], cwd: Path) -> subprocess.Popen:
    print(f"[{name}] {' '.join(command)}")
    return subprocess.Popen(command, cwd=cwd)


def stop_process(proc: subprocess.Popen) -> None:
    if proc.poll() is not None:
        return
    if os.name == "nt":
        proc.terminate()
    else:
        proc.send_signal(signal.SIGTERM)


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(line_buffering=True)

    print("=" * 44)
    print("  XJTU Canteen - start services")
    print("=" * 44)

    started: list[tuple[str, subprocess.Popen]] = []

    if is_port_open(BACKEND_HOST, BACKEND_PORT):
        print(f"[backend] reuse existing service at {BACKEND_URL}")
    else:
        started.append((
            "backend",
            start_process("backend", [command_path("mvn"), "spring-boot:run"], ROOT / "java-backend"),
        ))

    if is_port_open(FRONTEND_HOST, FRONTEND_PORT):
        print(f"[frontend] reuse existing service at {FRONTEND_URL}")
    else:
        started.append((
            "frontend",
            start_process("frontend", [command_path("npm"), "run", "dev", "--", "--host", FRONTEND_HOST], ROOT / "vue-frontend"),
        ))

    try:
        if not wait_for_port(FRONTEND_HOST, FRONTEND_PORT, timeout=20):
            print(f"\nFrontend did not become ready at {FRONTEND_URL}. Check the npm/Vite output above.")
            return 1

        print(f"\nbackend:  {BACKEND_URL}")
        print(f"frontend: {FRONTEND_URL}")
        if not is_port_open(BACKEND_HOST, BACKEND_PORT):
            print("Warning: backend is not reachable yet, so API calls may fail.")
        print("Press Ctrl+C to stop services started by this script.\n")
        webbrowser.open(FRONTEND_URL)

        while True:
            for name, proc in started:
                code = proc.poll()
                if code is not None:
                    print(f"[{name}] exited with code {code}")
                    return code if code != 0 else 1
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopping services started by this script...")
        return 0
    finally:
        for _, proc in started:
            stop_process(proc)


if __name__ == "__main__":
    sys.exit(main())
