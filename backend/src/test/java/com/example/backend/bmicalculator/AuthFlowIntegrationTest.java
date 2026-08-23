package com.example.backend.bmicalculator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end flow through the real security filter chain and an in-memory H2 database:
 * register -> login -> calculate (authenticated) -> history is scoped to that user.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLoginThenCalculate_endToEnd() throws Exception {
        String email = "integration+" + System.nanoTime() + "@test.com";
        String registerBody = """
                {"email":"%s","password":"secret123","firstName":"Int","lastName":"Test"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        String loginBody = """
                {"email":"%s","password":"secret123"}
                """.formatted(email);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();
        assertThat(token).isNotBlank();

        mockMvc.perform(post("/api/bmi/calculate")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"weight\":85,\"height\":175}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("OVERWEIGHT"));

        mockMvc.perform(get("/api/bmi/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weight").value(85.0));
    }

    @Test
    void calculate_withoutToken_isRejected() throws Exception {
        // No AuthenticationEntryPoint/httpBasic is configured, so Spring Security's
        // default stateless entry point (Http403ForbiddenEntryPoint) answers 403, not 401.
        mockMvc.perform(post("/api/bmi/calculate")
                        .contentType("application/json")
                        .content("{\"weight\":80,\"height\":180}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stats_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/bmi/stats"))
                .andExpect(status().isOk());
    }

    @Test
    void register_withDuplicateEmail_returnsConflict() throws Exception {
        String email = "dup+" + System.nanoTime() + "@test.com";
        String body = """
                {"email":"%s","password":"secret123","firstName":"Dup","lastName":"Test"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        String email = "wrongpass+" + System.nanoTime() + "@test.com";
        String registerBody = """
                {"email":"%s","password":"secret123","firstName":"Wrong","lastName":"Pass"}
                """.formatted(email);
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isCreated());

        String badLoginBody = """
                {"email":"%s","password":"wrong-password"}
                """.formatted(email);
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(badLoginBody))
                .andExpect(status().isUnauthorized());
    }
}
