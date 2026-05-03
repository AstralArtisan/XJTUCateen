# 西交食堂评价系统

一个基于 Spring Boot、Vue 3 和 MySQL 的校园食堂评价与推荐系统。项目包含用户端、管理后台、排行榜、举报治理和 AI 美食推荐能力。

## 功能概览

- 用户注册、登录、资料维护、密码修改
- 食堂、窗口、分类、标签浏览与筛选
- 窗口详情、评分评价、点赞、举报
- 收藏、黑名单、浏览历史和个人口味画像
- 今日推荐、个性化推荐、AI 多轮推荐
- 评分榜、热度榜、最新评价榜
- 管理后台：用户角色、食堂、窗口、标签、评论治理、数据看板

## 技术栈

- 后端：Java 17、Spring Boot 3.3、Spring JDBC、MyBatis、MySQL
- 前端：Vue 3、Vue Router、Pinia、Axios、Vite
- 测试：JUnit 5、Spring Boot Test、H2
- 辅助脚本：Python 一键启动脚本

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 18+ 和 npm
- MySQL 8+
- Python 3.10+

## 数据库配置

默认连接配置：

```text
数据库：xjtu_canteen
地址：127.0.0.1:3306
用户：root
密码：xjtuse
```

如需修改，在启动前设置环境变量：

```powershell
$env:MYSQL_URL="jdbc:mysql://127.0.0.1:3306/xjtu_canteen?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="你的密码"
```

后端启动时会自动执行建表脚本：

```text
java-backend/src/main/resources/db/schema-mysql.sql
```

该脚本会创建基础表，并插入默认管理员账号。

## 默认管理员

```text
账号：admin001
密码：123456
```

如果数据库中已经存在 `admin001`，初始化脚本不会覆盖该用户。

## 一键启动

在项目根目录执行：

```powershell
python .\start.py
```

脚本会启动或复用本机已有服务：

```text
前端：http://127.0.0.1:5173
后端：http://127.0.0.1:8000
```

如果 `8000` 或 `5173` 已有服务占用，脚本会复用已有服务。若由脚本启动的任一服务异常退出，脚本会返回失败状态并停止它启动的其它服务。

## 分别启动

启动后端：

```powershell
cd java-backend
mvn spring-boot:run
```

启动前端：

```powershell
cd vue-frontend
npm install
npm run dev
```

前端开发服务器默认通过 Vite 代理把 `/api` 转发到 `http://127.0.0.1:8000`。

## AI 推荐配置

系统支持 OpenAI-compatible 的 Chat Completions 接口。未配置 API Key 时，AI 推荐会自动降级为本地推荐。

ChatAnywhere 示例：

```powershell
$env:LLM_API_URL="https://api.chatanywhere.tech"
$env:LLM_API_KEY="你的 API Key"
$env:LLM_MODEL="gpt-3.5-turbo"
python .\start.py
```

如果只填写服务 host，例如 `https://api.chatanywhere.tech`，后端会自动补全为：

```text
https://api.chatanywhere.tech/v1/chat/completions
```

兼容旧配置：

```text
DEEPSEEK_API_KEY
DEEPSEEK_MODEL
```

不要把真实 API Key 提交到仓库。建议使用当前终端环境变量或本机私有配置。

## 常用测试命令

后端测试：

```powershell
cd java-backend
mvn test
```

前端生产构建：

```powershell
cd vue-frontend
npm run build
```

也可以在根目录运行测试辅助脚本：

```powershell
python .\run_tests.py
```

## 数据迁移

如果需要从旧 SQLite 数据迁移到 MySQL，先安装依赖：

```powershell
pip install pymysql
```

然后执行：

```powershell
python scripts/sqlite_to_mysql.py `
  --sqlite path/to/legacy-canteen.sqlite3 `
  --mysql-host 127.0.0.1 --mysql-port 3306 `
  --mysql-user root --mysql-password xjtuse --mysql-db xjtu_canteen
```

## 项目结构

```text
java-backend/   Spring Boot 后端
vue-frontend/  Vue 3 前端
scripts/       启动和数据迁移脚本
start.py       一键启动脚本
run_tests.py   测试辅助脚本
```

## 接口约定

- 后端 API 前缀：`/api`
- 响应格式：`{ code, message, data }`
- 登录鉴权：`Authorization: Bearer <token>`
- 密码哈希：PBKDF2-HMAC-SHA256
