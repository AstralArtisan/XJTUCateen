package com.example.canteen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterAndRejectDuplicateStudentNo() throws Exception {
        String registerPayload = """
                {
                  \"studentNo\": \"20239999\",
                  \"password\": \"password\",
                  \"nickname\": \"测试用户\"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String duplicatePayload = """
                {
                  \"studentNo\": \"20230001\",
                  \"password\": \"password\",
                  \"nickname\": \"重复\"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(duplicatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4003));
    }

    @Test
    void shouldLoginAndReadCurrentUser() throws Exception {
        String loginPayload = """
                {
                  \"studentNo\": \"20230001\",
                  \"password\": \"password\"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.studentNo").value("20230001"));
    }
}
