package com.example.backend.bmicalculator.service;

import com.example.backend.bmicalculator.entity.User;
import com.example.backend.bmicalculator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void findById_returnsUser_whenFound() {
        User user = new User("john@doe.com", "hash", "John", "Doe");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.findById(1L)).isSameAs(user);
    }

    @Test
    void findById_throws404_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    void updateUser_updatesNameFields_butNotEmail() {
        User existing = new User("john@doe.com", "hash", "John", "Doe");
        existing.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updates = new User();
        updates.setFirstName("Johnny");
        updates.setLastName("Doeson");

        User result = userService.updateUser(1L, updates);

        assertThat(result.getFirstName()).isEqualTo("Johnny");
        assertThat(result.getLastName()).isEqualTo("Doeson");
        assertThat(result.getEmail()).isEqualTo("john@doe.com");
    }

    @Test
    void deleteUser_softDeletesByDeactivating() {
        User existing = new User("john@doe.com", "hash", "John", "Doe");
        existing.setId(1L);
        existing.setIsActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.deleteUser(1L);

        verify(userRepository).save(argThat(u -> !u.getIsActive()));
    }
}
