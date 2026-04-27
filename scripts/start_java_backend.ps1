param(
  [string]$MySqlUrl = "jdbc:mysql://127.0.0.1:3306/xjtu_canteen?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
  [string]$MySqlUser = "root",
  [string]$MySqlPassword = "xjtuse"
)

$env:MYSQL_URL = $MySqlUrl
$env:MYSQL_USER = $MySqlUser
$env:MYSQL_PASSWORD = $MySqlPassword

Push-Location "java-backend"
try {
  mvn spring-boot:run
} finally {
  Pop-Location
}
