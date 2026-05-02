"""Migrate SQLite data from legacy backend to MySQL for java-backend.

Usage:
  python scripts/sqlite_to_mysql.py \
    --sqlite path/to/legacy-canteen.sqlite3 \
    --mysql-host 127.0.0.1 --mysql-port 3306 \
    --mysql-user root --mysql-password xjtuse --mysql-db xjtu_canteen
"""

from __future__ import annotations

import argparse
import sqlite3
from typing import Any

import pymysql

TABLES = [
    "user",
    "canteen",
    "stall",
    "tag",
    "stall_tag",
    "review",
    "favorite",
    "blacklist",
    "history",
]


def read_rows(sqlite_path: str, table: str) -> tuple[list[str], list[tuple[Any, ...]]]:
    conn = sqlite3.connect(sqlite_path)
    conn.row_factory = sqlite3.Row
    try:
        rows = conn.execute(f"SELECT * FROM {table}").fetchall()
        if not rows:
            cols = [r[1] for r in conn.execute(f"PRAGMA table_info({table})").fetchall()]
            return cols, []
        cols = list(rows[0].keys())
        return cols, [tuple(r[c] for c in cols) for r in rows]
    finally:
        conn.close()


def import_rows(mysql_conn, table: str, cols: list[str], rows: list[tuple[Any, ...]]) -> None:
    if not rows:
        print(f"[skip] {table}: 0 rows")
        return
    placeholders = ",".join(["%s"] * len(cols))
    col_sql = ",".join([f"`{c}`" for c in cols])
    sql = f"INSERT INTO `{table}` ({col_sql}) VALUES ({placeholders})"

    with mysql_conn.cursor() as cur:
        cur.executemany(sql, rows)
    print(f"[ok] {table}: {len(rows)} rows")


def reset_tables(mysql_conn) -> None:
    with mysql_conn.cursor() as cur:
        cur.execute("SET FOREIGN_KEY_CHECKS = 0")
        for table in reversed(TABLES):
            cur.execute(f"TRUNCATE TABLE `{table}`")
        cur.execute("SET FOREIGN_KEY_CHECKS = 1")
    mysql_conn.commit()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sqlite", required=True)
    parser.add_argument("--mysql-host", default="127.0.0.1")
    parser.add_argument("--mysql-port", type=int, default=3306)
    parser.add_argument("--mysql-user", default="root")
    parser.add_argument("--mysql-password", default="xjtuse")
    parser.add_argument("--mysql-db", default="xjtu_canteen")
    args = parser.parse_args()

    mysql_conn = pymysql.connect(
        host=args.mysql_host,
        port=args.mysql_port,
        user=args.mysql_user,
        password=args.mysql_password,
        db=args.mysql_db,
        charset="utf8mb4",
        autocommit=False,
    )

    try:
        reset_tables(mysql_conn)
        for table in TABLES:
            cols, rows = read_rows(args.sqlite, table)
            import_rows(mysql_conn, table, cols, rows)
        mysql_conn.commit()
    finally:
        mysql_conn.close()

    print("migration completed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
