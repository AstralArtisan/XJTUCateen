"""
Run the current Java backend and Vue frontend verification suite.

Usage:
    python run_tests.py
"""
from __future__ import annotations

import subprocess
import sys
from shutil import which
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def run_step(name: str, command: list[str], cwd: Path) -> int:
    print("=" * 60)
    print(f"  {name}")
    print("=" * 60)
    executable = which(command[0])
    if executable is None:
        print(f"[FAIL] Command not found: {command[0]}")
        return 127
    result = subprocess.run([executable, *command[1:]], cwd=cwd)
    print()
    if result.returncode == 0:
        print(f"[OK] {name}")
    else:
        print(f"[FAIL] {name}")
    print()
    return result.returncode


def main() -> int:
    steps = [
        ("Java backend tests", ["mvn", "test"], ROOT / "java-backend"),
        ("Vue frontend build", ["npm", "run", "build"], ROOT / "vue-frontend"),
    ]

    for name, command, cwd in steps:
        code = run_step(name, command, cwd)
        if code != 0:
            return code

    print("All checks passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
