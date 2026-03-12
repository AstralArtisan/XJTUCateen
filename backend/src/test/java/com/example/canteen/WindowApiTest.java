package com.example.canteen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WindowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSearchWindowByKeyword() throws Exception {
        mockMvc.perform(get("/api/windows").param("keyword", "Ramen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldGetWindowDetail() throws Exception {
        mockMvc.perform(get("/api/windows/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.windowName").value("North Noodles"));
    }

    @Test
    void shouldLoadTopRatedRanking() throws Exception {
        mockMvc.perform(get("/api/rankings/top-rated").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(3));
    }
}
