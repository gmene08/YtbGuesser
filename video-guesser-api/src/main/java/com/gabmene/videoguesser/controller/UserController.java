package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.UserResponseDTO;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.findAll();

        if(users.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        List<UserResponseDTO> userResponseDTOS = users.stream().map(UserResponseDTO::from).toList();
        return ResponseEntity.ok(userResponseDTOS);
    }

    @PostMapping("/guest")
    public ResponseEntity<UserResponseDTO> createGuest(@RequestBody User user) {
        User savedUser = userService.createGuest(user);
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

}
