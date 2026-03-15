# School Canteen Rating & Recommendation System (MVP)

A course-project level web application for student login, canteen window search, rating/review, ranking display, and future recommendation-related expansion.

## Tech Stack
- Backend: Java 17, Spring Boot 3, Spring Data JPA, Spring Validation
- Password Hash: BCrypt (`spring-security-crypto`)
- Frontend: Thymeleaf + vanilla JavaScript + CSS
- Database: MySQL 8
- Build Tool: Maven Wrapper (`mvnw`)

## Project Structure
```text
project/
|-- backend/                 # Spring Boot application
|-- frontend/                # Reserved for future Vue split
|-- sql/                     # schema and demo data
|-- docs/                    # API and DB docs
|-- docker-compose.yml       # MySQL startup
`-- README.md
```

## Quick Start

### 1) Start MySQL
```bash
docker compose up -d
```

Default DB config:
- Host: `localhost`
- Port: `3306`
- DB: `canteen_mvp`
- User: `root`
- Password: `root123`

### 2) Start backend (Recommended)

Windows:
```powershell
cd backend
.\run-dev.cmd
```

Linux/macOS:
```bash
cd backend
chmod +x run-dev.sh
./run-dev.sh
```

Alternative (when your project path is ASCII-only):
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Open:
- Home: [http://localhost:8080](http://localhost:8080)
- Login: [http://localhost:8080/login](http://localhost:8080/login)

## Demo Accounts
All preloaded users use password `password`:
- `20230001`
- `20230002`
- `20230003`

## Main Features (MVP)
- Register with `studentNo + password` (duplicate student number check)
- Login/logout/current user with Session
- Search windows by keyword/canteen/cuisine type
- View window detail + review list
- Submit/update rating and review (1 user 1 review per window)
- Rankings: top-rated, hottest, latest reviews

## Tests
Windows:
```powershell
cd backend
.\mvnw.cmd test
```

Linux/macOS:
```bash
cd backend
./mvnw test
```

## Initialization
- `sql/schema.sql`
- `sql/data.sql`

MySQL imports these scripts automatically only on first container creation, via `/docker-entrypoint-initdb.d`.
This means data created later in the app, such as registered users and submitted reviews, will be preserved across app restarts.

To fully reset the database and re-import demo data:

Windows PowerShell:
```powershell
docker compose down
Remove-Item -Recurse -Force .\mysql-data
docker compose up -d
```

Linux/macOS:
```bash
docker compose down
rm -rf ./mysql-data
docker compose up -d
```

## Future Extensions
- Personalized recommendation (`recommendation_seed` reserved)
- Random recommendation with blacklist filtering
- Favorite/blacklist APIs (`favorite_window`, `blocked_window`)
- Share tracking (`share_record`)
- Admin maintenance for canteen windows

## Troubleshooting
1. DB connection fails: check Docker Desktop status and 3306 port conflicts.
2. App startup fails on first run: verify Docker can mount `sql/schema.sql` and `sql/data.sql` into MySQL.
3. If Maven is not installed, always use `mvnw` / `mvnw.cmd`.

## Known Issue (Windows + non-ASCII path)
If your project path contains non-ASCII characters (for example Chinese), `mvnw spring-boot:run` may fail with:
`ClassNotFoundException: com.example.canteen.CanteenApplication`

Use `run-dev.cmd` / `run-dev.sh` (package + `java -jar`) to avoid this issue.
