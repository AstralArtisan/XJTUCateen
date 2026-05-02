package com.xjtu.canteen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ApiControllerMockMvcTest extends CanteenTestBase {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void authEndpointsRegisterLoginAndRejectMissingToken() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("student_id", "s001", "username", "用户S1", "password", "pass"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("student_id", "s001", "password", "pass"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.user.student_id").value("s001"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("student_id", "s001", "password", "wrong"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(4010));

        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void stallReviewRankingAndSecurityEndpointsFollowApiContract() throws Exception {
        Long cid = insertCanteen("系统测试食堂");
        Long sid = insertStall(cid, "系统测试窗口", "快餐");
        String token = registerAndLogin("rev001", "pass", "评论用户");

        mvc.perform(get("/api/stalls"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list").isArray())
            .andExpect(jsonPath("$.data.total").exists());

        mvc.perform(get("/api/canteens"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list").isArray());

        mvc.perform(get("/api/stalls/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(4004));

        mvc.perform(get("/api/stalls").param("keyword", "系统"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mvc.perform(post("/api/reviews").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("stall_id", sid, "rating", 5, "content", "好"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(4001));

        mvc.perform(post("/api/reviews").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("stall_id", sid, "rating", 4, "content", "不错"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mvc.perform(post("/api/reviews").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("stall_id", sid, "rating", 6, "content", "超出范围"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(4010));

        mvc.perform(get("/api/stalls/{id}/reviews", sid))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list").isArray());

        mvc.perform(get("/api/rankings/score"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list").isArray());
        mvc.perform(get("/api/rankings/hot"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
        mvc.perform(get("/api/rankings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mvc.perform(post("/api/admin/stalls").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("canteen_id", cid, "name", "非法窗口"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(4001));

        mvc.perform(post("/api/admin/stalls").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("canteen_id", cid, "name", "越权窗口", "category", "快餐"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(4003));

        mvc.perform(post("/api/users/me/favorites").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("stall_id", sid))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void adminUserCanCreateStallAndDeleteReview() throws Exception {
        Long cid = insertCanteen("后台食堂");
        Long sid = insertStall(cid, "后台窗口", "快餐");
        Long uid = insertUser("admin001", "管理员", 1);
        String adminToken = login("admin001", "123456");
        String userToken = registerAndLogin("user001", "pass", "普通用户");

        mvc.perform(post("/api/reviews").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("stall_id", sid, "rating", 5, "content", "好评"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
        Long reviewId = jdbc.queryForObject("SELECT id FROM review WHERE user_id <> ?", Long.class, uid);

        mvc.perform(delete("/api/admin/reviews/{id}", reviewId).header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mvc.perform(post("/api/admin/stalls").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("canteen_id", cid, "name", "管理员新增窗口", "category", "小吃"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("管理员新增窗口"));
    }

    private String registerAndLogin(String studentId, String password, String username) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("student_id", studentId, "username", username, "password", password))));
        return login(studentId, password);
    }

    private String login(String studentId, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("student_id", studentId, "password", password))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Map<?, ?> data = (Map<?, ?>) mapper.readValue(body, Map.class).get("data");
        return String.valueOf(data.get("token"));
    }

    private String json(Object value) throws Exception {
        return mapper.writeValueAsString(value);
    }
}
