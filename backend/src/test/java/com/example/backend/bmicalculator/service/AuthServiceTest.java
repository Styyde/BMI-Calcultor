package com.example.backend.bmicalculator.service;

import com.example.backend.bmicalculator.dto.auth.AuthRequest;
import com.example.backend.bmicalculator.dto.auth.AuthResponse;
import com.example.backend.bmicalculator.dto.auth.RegisterRequest;
import com.example.backend.bmicalculator.entity.Role;
import com.example.backend.bmicalculator.entity.User;
import com.example.backend.bmicalculator.repository.UserRepository;
import com.example.backend.bmicalculator.security.JwtTokenProvider;
import com.example.backend.bmicalculator.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider, authenticationManager);
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_createsUser_andReturnsToken() {
        RegisterRequest request = new RegisterRequest("new@user.com", "secret1", "New", "User");
        when(userRepository.existsByEmail("new@user.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            u.setRole(Role.USER);
            return u;
        });
        when(tokenProvider.generateTokenFromEmail("new@user.com", 1L, "USER")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("new@user.com");
        assertThat(response.getType()).isEqualTo("Bearer");

        verify(userRepository).save(argThat(u -> u.getPassword().equals("hashed")));
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("dup@user.com", "secret1", "Dup", "User");
        when(userRepository.existsByEmail("dup@user.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email déjà utilisé");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_delegatesToAuthenticationManager_andReturnsToken() {
        AuthRequest request = new AuthRequest("jane@doe.com", "secret1");
        UserPrincipal principal = new UserPrincipal(5L, "jane@doe.com", "hashed", "Jane", "Doe", "USER", null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("jane@doe.com");
        assertThat(response.getId()).isEqualTo(5L);
    }

    @Test
    void login_propagatesBadCredentials() {
        AuthRequest request = new AuthRequest("jane@doe.com", "wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_rejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> authService.refreshToken(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token manquant");

        assertThatThrownBy(() -> authService.refreshToken("NotBearer xyz"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void refreshToken_rejectsInvalidJwt() {
        when(tokenProvider.parseClaims("bad-token")).thenThrow(new JwtException("bad"));

        assertThatThrownBy(() -> authService.refreshToken("Bearer bad-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token invalide");
    }

    @Test
    void refreshToken_rejectsWhenUserNoLongerExists() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("jane@doe.com");
        when(claims.get("id", Long.class)).thenReturn(9L);
        when(tokenProvider.parseClaims("good-token")).thenReturn(claims);
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("Bearer good-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    void refreshToken_issuesNewToken_whenClaimsMatchUser() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("jane@doe.com");
        when(claims.get("id", Long.class)).thenReturn(9L);
        when(tokenProvider.parseClaims("good-token")).thenReturn(claims);

        User user = new User("jane@doe.com", "hashed", "Jane", "Doe");
        user.setId(9L);
        user.setRole(Role.USER);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(tokenProvider.generateTokenFromEmail("jane@doe.com", 9L, "USER")).thenReturn("new-jwt");

        AuthResponse response = authService.refreshToken("Bearer good-token");

        assertThat(response.getToken()).isEqualTo("new-jwt");
        assertThat(response.getEmail()).isEqualTo("jane@doe.com");
    }
}
