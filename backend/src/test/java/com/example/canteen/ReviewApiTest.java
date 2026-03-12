package com.example.canteen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSubmitReviewWhenLoggedIn() throws Exception {
        String loginPayload = """
                {
                  \"studentNo\": \"20230001\",
                  \"password\": \"password\"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        String reviewPayload = """
                {
                  \"score\": 5,
                  \"commentText\": \"测试更新评论\"
                }
                """;

        mockMvc.perform(post("/api/windows/1/reviews")
                        .session(session)
                        .contentType("application/json")
                        .content(reviewPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.score").value(5));
    }

    @Test
    void shouldRejectReviewWhenNotLoggedIn() throws Exception {
        String reviewPayload = """
                {
                  \"score\": 4,
                  \"commentText\": \"未登录评论\"
                }
                """;

        mockMvc.perform(post("/api/windows/1/reviews")
                        .contentType("application/json")
                        .content(reviewPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }
}
