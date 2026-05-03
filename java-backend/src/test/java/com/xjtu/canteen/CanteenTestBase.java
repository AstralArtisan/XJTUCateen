package com.xjtu.canteen;

import com.xjtu.canteen.security.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class CanteenTestBase {
    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String table : new String[]{"history", "blacklist", "favorite", "review_report", "review_like", "review", "stall_tag", "tag", "stall", "canteen", "user"}) {
            jdbc.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY");
        }
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    protected Long insertCanteen(String name) {
        jdbc.update("INSERT INTO canteen (name, location, description) VALUES (?, ?, ?)", name, "测试校区", "测试食堂");
        return jdbc.queryForObject("SELECT id FROM canteen WHERE name = ?", Long.class, name);
    }

    protected Long insertStall(Long canteenId, String name, String category) {
        jdbc.update(
            "INSERT INTO stall (canteen_id, name, category, description, status) VALUES (?, ?, ?, ?, 1)",
            canteenId, name, category, "测试描述"
        );
        return jdbc.queryForObject("SELECT id FROM stall WHERE canteen_id = ? AND name = ?", Long.class, canteenId, name);
    }

    protected Long insertUser(String studentId, String username, int role) {
        jdbc.update(
            "INSERT INTO user (student_id, username, password_hash, role, status) VALUES (?, ?, ?, ?, 1)",
            studentId, username, PasswordUtil.hashPassword("123456"), role
        );
        return jdbc.queryForObject("SELECT id FROM user WHERE student_id = ?", Long.class, studentId);
    }
}
