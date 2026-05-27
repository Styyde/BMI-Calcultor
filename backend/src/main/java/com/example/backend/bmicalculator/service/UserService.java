package com.example.backend.bmicalculator.service;

import com.example.backend.bmicalculator.entity.User;
import com.example.backend.bmicalculator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User updateUser(Long id, User userDetails) {
        User user = findById(id);
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        // Ne pas permettre de changer l'email directement pour des raisons de sécurité
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        user.setIsActive(false);  // Soft delete
        userRepository.save(user);
    }
}