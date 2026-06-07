package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.UserResponseDTO;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.listener.UserSessionManager;
import com.gabmene.videoguesser.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserSessionManager userSessionManager;

    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.findAll();

        if(users.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        List<UserResponseDTO> userResponseDTOS = users.stream().map(UserResponseDTO::from).toList();
        return ResponseEntity.ok(userResponseDTOS);
    }

    @PostMapping("/guest")
    public ResponseEntity<UserResponseDTO> createGuest(@RequestBody User user, HttpServletResponse response) {
        User savedUser = userService.createGuest(user, response);
        userSessionManager.scheduleInitialDestruction(savedUser.getId());
        return ResponseEntity.ok(UserResponseDTO.from(savedUser));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.ok(UserResponseDTO.from(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> loginUser(@RequestBody User user) {
        User userLogin = userService.loginUser(user.getNickname(), user.getPassword());
        return ResponseEntity.ok(UserResponseDTO.from(userLogin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        User user = userService.findUserById(id);
        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Principal principal, HttpServletResponse response) {
        User user = userService.refreshUserCookie(principal, response);
        userSessionManager.resetUserDeletionTimer(user.getId());
        return ResponseEntity.ok(UserResponseDTO.from(user));
    }

    @DeleteMapping("/{id}/logout")
    public ResponseEntity<Void> logout(@PathVariable Integer id, HttpServletResponse response) {
        userSessionManager.clearAllPendingUserTimers(id);
        userService.userLogout(id ,response);
        return ResponseEntity.ok().build();
    }


}
