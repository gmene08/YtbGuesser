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

    public User loginUser(String nickname, String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hashedPassword = encoder.encode(password);
        User user= userRepository.findByNickname(nickname).orElseThrow(()-> new RuntimeException("Wrong nickname"));
        if(encoder.matches(password, user.getPassword())){
            System.out.println("User: " + user.getNickname() +" logged in successfully");
            return user;
        }else{
            throw new RuntimeException("Wrong password");
        }
    }
}
