"""Start the Java backend and Vue frontend together.

Usage:
    python start.py
"""
from __future__ import annotations

import os
import signal
import subprocess
import sys
import time
import webbrowser
from pathlib import Path


ROOT = Path(__file__).resolve().parent
BACKEND_URL = "http://127.0.0.1:8000"
FRONTEND_URL = "http://127.0.0.1:5173"


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
    print("=" * 44)
    print("  西交食堂评价系统 - 一键启动")
    print("=" * 44)

    processes = [
        start_process("后端", ["mvn", "spring-boot:run"], ROOT / "java-backend"),
        start_process("前端", ["npm", "run", "dev"], ROOT / "vue-frontend"),
    ]

    try:
        time.sleep(2)
        print(f"\n后端: {BACKEND_URL}")
        print(f"前端: {FRONTEND_URL}")
        print("按 Ctrl+C 停止服务。\n")
        webbrowser.open(FRONTEND_URL)

        while all(proc.poll() is None for proc in processes):
            time.sleep(1)
        return next((proc.returncode for proc in processes if proc.returncode), 0)
    except KeyboardInterrupt:
        print("\n正在停止服务...")
        return 0
    finally:
        for proc in processes:
            stop_process(proc)


if __name__ == "__main__":
    sys.exit(main())
