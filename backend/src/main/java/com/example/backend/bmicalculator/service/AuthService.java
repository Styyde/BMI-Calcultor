package com.example.backend.bmicalculator.service;

import com.example.backend.bmicalculator.dto.auth.AuthRequest;
import com.example.backend.bmicalculator.dto.auth.AuthResponse;
import com.example.backend.bmicalculator.dto.auth.RegisterRequest;
import com.example.backend.bmicalculator.entity.User;
import com.example.backend.bmicalculator.repository.UserRepository;
import com.example.backend.bmicalculator.security.JwtTokenProvider;
import com.example.backend.bmicalculator.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        // ✅ Création sans builder (car Lombok ne fonctionne pas)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromEmail(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name()
        );

        // ✅ Création sans builder
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setRole(savedUser.getRole().name());
        response.setType("Bearer");

        return response;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setId(userPrincipal.getId());
        response.setEmail(userPrincipal.getEmail());
        response.setFirstName(userPrincipal.getFirstName());
        response.setLastName(userPrincipal.getLastName());
        response.setRole(userPrincipal.getRole());
        response.setType("Bearer");

        return response;
    }

    public AuthResponse refreshToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token manquant ou invalide");
        }

        String token = authorizationHeader.substring(7);

        Claims claims;
        try {
            claims = tokenProvider.parseClaims(token);
        } catch (JwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalide");
        }

        String email = claims.getSubject();
        Long userId = claims.get("id", Long.class);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));

        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalide");
        }

        String newToken = tokenProvider.generateTokenFromEmail(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        AuthResponse response = new AuthResponse();
        response.setToken(newToken);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole().name());
        response.setType("Bearer");

        return response;
    }
}