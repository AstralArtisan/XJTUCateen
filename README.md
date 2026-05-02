# Java + Vue3 + MySQL Migration Guide

## 1. Start Java Backend

```bash
cd java-backend
mvn spring-boot:run
```

Environment variables:
- `MYSQL_URL` (default: `jdbc:mysql://127.0.0.1:3306/xjtu_canteen?...`)
- `MYSQL_USER` (default: `root`)
- `MYSQL_PASSWORD` (default: `xjtuse`)
- `CANTEEN_TOKEN_SECRET` (default: `xjtu_canteen_secret`)
- `DEEPSEEK_API_KEY` (optional, for AI recommendation)
- `DEEPSEEK_MODEL` (optional, default `deepseek-chat`)

Backend exposes API at `http://127.0.0.1:8000/api/**`.

## 2. Migrate Data from SQLite to MySQL

Install dependency once:

```bash
pip install pymysql
```

Run migration:

```bash
python scripts/sqlite_to_mysql.py \
  --sqlite path/to/legacy-canteen.sqlite3 \
  --mysql-host 127.0.0.1 --mysql-port 3306 \
  --mysql-user root --mysql-password xjtuse --mysql-db xjtu_canteen
```

## 3. Start Vue3 Frontend

```bash
cd vue-frontend
npm install
npm run dev
```

Frontend runs at `http://127.0.0.1:5173` and proxies `/api` to `http://127.0.0.1:8000`.

## 4. Compatibility Notes

- API routes keep the original `/api/**` contract.
- Response envelope remains `{ code, message, data }`.
- Token format keeps backward compatibility with previous custom HMAC token.
- Password hash algorithm remains PBKDF2-HMAC-SHA256 (120000 iterations).
