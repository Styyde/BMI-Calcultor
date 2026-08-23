package com.example.backend.bmicalculator.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 86_400_000);
    }

    @Test
    void generateTokenFromEmail_producesTokenReadableByProvider() {
        String token = tokenProvider.generateTokenFromEmail("jane@doe.com", 7L, "USER");

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("jane@doe.com");
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(7L);

        Claims claims = tokenProvider.parseClaims(token);
        assertThat(tokenProvider.getRoleFromClaims(claims)).isEqualTo("USER");
    }

    @Test
    void validateToken_returnsFalse_forGarbageInput() {
        assertThat(tokenProvider.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forExpiredToken() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", -1000);
        String expiredToken = tokenProvider.generateTokenFromEmail("jane@doe.com", 7L, "USER");

        assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void parseClaims_stillReturnsClaims_forExpiredToken() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", -1000);
        String expiredToken = tokenProvider.generateTokenFromEmail("jane@doe.com", 7L, "USER");

        Claims claims = tokenProvider.parseClaims(expiredToken);

        assertThat(claims.getSubject()).isEqualTo("jane@doe.com");
    }
}
