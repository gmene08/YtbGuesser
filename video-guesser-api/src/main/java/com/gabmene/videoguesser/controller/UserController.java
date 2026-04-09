package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.UserResponseDTO;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.service.UserService;
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

        List<UserResponseDTO> userResponseDTOS = users.stream().map(UserResponseDTO::new).toList();
        return ResponseEntity.ok(userResponseDTOS);
    }

    @PostMapping("/guest")
    public ResponseEntity<UserResponseDTO> createGuest(@RequestBody User user) {
        user.setIsGuest(true);
        User savedUser = userService.save(user);
        return ResponseEntity.ok(new UserResponseDTO(savedUser));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody User user) {
        user.setIsGuest(false);
        User savedUser = userService.save(user);
        return ResponseEntity.ok(new UserResponseDTO(savedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        User user = userService.findUserById(id);
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

}
