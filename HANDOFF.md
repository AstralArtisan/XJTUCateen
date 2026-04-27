# Handoff Note

## 1. Current goal
- Complete the migration verification from legacy Python/SQLite + vanilla JS to `Java Spring Boot + MySQL + Vue3`.
- Finish validating key user/admin flows and fix remaining frontend/runtime issues.

## 2. What has been implemented
- Added new Java backend under `java-backend/` with Spring Boot, JDBC/MyBatis dependency setup, MySQL schema init, custom token/password compatibility, and API-compatible controllers/services.
- Added new Vue3 frontend under `vue-frontend/` with Vite, Pinia, Vue Router, Axios, and core pages: home, login, register, stall detail, rankings, profile, admin.
- Added SQLite -> MySQL migration tooling in `scripts/sqlite_to_mysql.py`.
- Fixed BOM/encoding issues in Java sources, SQL schema, and Vue frontend files.
- Fixed frontend API error handling in `vue-frontend/src/api/client.js` so backend `4xx` business responses are returned to page logic instead of throwing unhandled Axios errors.
- Built backend successfully with Maven, started backend successfully, created MySQL DB `xjtu_canteen`, migrated seed data successfully, and started Vite frontend successfully.

## 3. Key decisions made
- Preserve existing API contract: `/api/**`, `Authorization: Bearer ...`, response shape `{ code, message, data }`.
- Keep custom Python-era token format and PBKDF2 password hashing for compatibility.
- Use minimal fixes only; avoid broad refactors.
- Verify flows with real browser automation via Playwright CLI instead of adding test files.

## 4. Important files / modules involved
- Backend:
  - [pom.xml](/E:/Download/Code/XJTUCanteen/java-backend/pom.xml)
  - [application.yml](/E:/Download/Code/XJTUCanteen/java-backend/src/main/resources/application.yml)
  - [schema-mysql.sql](/E:/Download/Code/XJTUCanteen/java-backend/src/main/resources/db/schema-mysql.sql)
  - [ApiController.java](/E:/Download/Code/XJTUCanteen/java-backend/src/main/java/com/xjtu/canteen/controller/ApiController.java)
  - [CoreService.java](/E:/Download/Code/XJTUCanteen/java-backend/src/main/java/com/xjtu/canteen/service/CoreService.java)
  - [LlmService.java](/E:/Download/Code/XJTUCanteen/java-backend/src/main/java/com/xjtu/canteen/service/LlmService.java)
  - [TokenUtil.java](/E:/Download/Code/XJTUCanteen/java-backend/src/main/java/com/xjtu/canteen/security/TokenUtil.java)
  - [PasswordUtil.java](/E:/Download/Code/XJTUCanteen/java-backend/src/main/java/com/xjtu/canteen/security/PasswordUtil.java)
- Frontend:
  - [client.js](/E:/Download/Code/XJTUCanteen/vue-frontend/src/api/client.js)
  - [LoginView.vue](/E:/Download/Code/XJTUCanteen/vue-frontend/src/views/LoginView.vue)
  - [ProfileView.vue](/E:/Download/Code/XJTUCanteen/vue-frontend/src/views/ProfileView.vue)
  - [AdminView.vue](/E:/Download/Code/XJTUCanteen/vue-frontend/src/views/AdminView.vue)
- Migration:
  - [sqlite_to_mysql.py](/E:/Download/Code/XJTUCanteen/scripts/sqlite_to_mysql.py)
  - [README_MIGRATION.md](/E:/Download/Code/XJTUCanteen/README_MIGRATION.md)

## 5. Known issues / blockers
- Remaining browser console noise:
  - `favicon.ico` 404 on frontend
  - Login page input is not wrapped in a `<form>`; browser warns `Password field is not contained in a form`
- Functional verification so far covered page load/login/read flows. Write flows are not fully verified yet.
- Data migration source SQLite had been zero-byte at one point; it was regenerated with `init_db()` and then migrated. Current MySQL data is seed data plus any verification interactions.

## 6. Next 3 concrete steps
1. Verify write flows end-to-end:
   - submit review
   - save profile
   - change password
   - admin create tag
2. Fix small frontend issues:
   - add `favicon.ico`
   - wrap login inputs/button in a real `<form>` and submit handler
3. Re-run browser verification after fixes and summarize any remaining gaps.
