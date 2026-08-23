package com.example.backend.bmicalculator.controller;

import com.example.backend.bmicalculator.dto.BmiResponse;
import com.example.backend.bmicalculator.entity.Role;
import com.example.backend.bmicalculator.entity.User;
import com.example.backend.bmicalculator.repository.projection.BmiStatsProjection;
import com.example.backend.bmicalculator.security.JwtAuthenticationFilter;
import com.example.backend.bmicalculator.security.JwtTokenProvider;
import com.example.backend.bmicalculator.security.UserPrincipal;
import com.example.backend.bmicalculator.service.BmiService;
import com.example.backend.bmicalculator.service.CustomUserDetailsService;
import com.example.backend.bmicalculator.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BmiController.class)
@AutoConfigureMockMvc(addFilters = false)
class BmiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BmiService bmiService;

    @MockBean
    private UserService userService;

    // Beans required to bootstrap SecurityConfig on the WebMvcTest slice.
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private BmiResponse sampleResponse() {
        return BmiResponse.builder()
                .weight(70.0)
                .height(175.0)
                .bmi(22.9)
                .category("NORMAL")
                .label("Poids normal")
                .advice("Excellent !")
                .color("#2ecc71")
                .minIdealWeight(56.7)
                .maxIdealWeight(76.3)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void calculate_returnsBmiResponse_forAnonymousRequest() throws Exception {
        when(bmiService.calculateAndSave(any(), any(), isNull())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/bmi/calculate")
                        .contentType("application/json")
                        .content("{\"weight\":70,\"height\":175}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(22.9))
                .andExpect(jsonPath("$.category").value("NORMAL"));
    }

    @Test
    void calculate_rejectsInvalidWeight() throws Exception {
        mockMvc.perform(post("/api/bmi/calculate")
                        .contentType("application/json")
                        .content("{\"weight\":5,\"height\":175}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.weight").exists());
    }

    @Test
    void calculate_passesAuthenticatedUser_toService() throws Exception {
        User user = new User("john@doe.com", "hash", "John", "Doe");
        user.setId(3L);
        user.setRole(Role.USER);
        UserPrincipal principal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(userService.findById(3L)).thenReturn(user);
        when(bmiService.calculateAndSave(any(), any(), eq(user))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/bmi/calculate")
                        .contentType("application/json")
                        .content("{\"weight\":70,\"height\":175}"))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_returnsListFromService() throws Exception {
        when(bmiService.getHistory(any(), isNull(), anyInt())).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/bmi/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("NORMAL"));
    }

    @Test
    void getStats_returnsAggregatedStats() throws Exception {
        BmiStatsProjection projection = new BmiStatsProjection() {
            public String getCategory() { return "NORMAL"; }
            public Long getCount() { return 5L; }
            public Double getAvgBmi() { return 23.1; }
            public Double getMinBmi() { return 19.0; }
            public Double getMaxBmi() { return 24.8; }
        };
        when(bmiService.getStats()).thenReturn(List.of(projection));

        mockMvc.perform(get("/api/bmi/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("NORMAL"))
                .andExpect(jsonPath("$[0].count").value(5));
    }
}
