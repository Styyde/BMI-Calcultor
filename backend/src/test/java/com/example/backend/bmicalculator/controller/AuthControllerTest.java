package com.example.backend.bmicalculator.controller;

import com.example.backend.bmicalculator.dto.auth.AuthResponse;
import com.example.backend.bmicalculator.security.JwtAuthenticationFilter;
import com.example.backend.bmicalculator.security.JwtTokenProvider;
import com.example.backend.bmicalculator.service.AuthService;
import com.example.backend.bmicalculator.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_returns201_withToken() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setToken("jwt-token");
        response.setEmail("new@user.com");
        response.setType("Bearer");
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"email\":\"new@user.com\",\"password\":\"secret1\",\"firstName\":\"New\",\"lastName\":\"User\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_rejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"email\":\"not-an-email\",\"password\":\"123\",\"firstName\":\"\",\"lastName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returnsConflict_whenEmailAlreadyUsed() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"email\":\"dup@user.com\",\"password\":\"secret1\",\"firstName\":\"A\",\"lastName\":\"B\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200_withToken() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setToken("jwt-token");
        response.setEmail("jane@doe.com");
        response.setType("Bearer");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"jane@doe.com\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@doe.com"));
    }

    @Test
    void login_returnsUnauthorized_forBadCredentials() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"jane@doe.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
}
