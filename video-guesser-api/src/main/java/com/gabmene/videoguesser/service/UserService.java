package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }


    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new RuntimeException("Nickname already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        user.setIsGuest(false);
        return userRepository.save(user);
    }

    @Transactional
    public User createGuest(User user) {
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new RuntimeException("Nickname already exists");
        }
        user.setIsGuest(true);
        return userRepository.save(user);
    }

    public User loginUser(String nickname, String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user= userRepository.findByNickname(nickname).orElseThrow(()-> new RuntimeException("Wrong nickname"));
        if(encoder.matches(password, user.getPassword())){
            System.out.println("User: " + user.getNickname() +" logged in successfully");
            return user;
        }else{
            throw new RuntimeException("Wrong password");
        }
    }
}
