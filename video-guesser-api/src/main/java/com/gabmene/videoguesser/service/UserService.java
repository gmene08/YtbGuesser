package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ConflictException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoomService roomService;

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }


    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new ConflictException("Nickname already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        user.setIsGuest(false);
        return userRepository.save(user);
    }

    @Transactional
    public User createGuest(User user, HttpServletResponse response) {
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new ConflictException("Nickname already exists");
        }
        if( user.getNickname() == null || user.getNickname().isBlank() ||
                user.getNickname().length() < AppConstants.MIN_NICKNAME_LENGTH ||
                user.getNickname().length() > AppConstants.MAX_NICKNAME_LENGTH) {
            throw new BusinessException("Nickname must be between 3 and 16 characters");
        }

        user.setIsGuest(true);
        User savedGuest = userRepository.save(user);

        // generate token
        jwtService.applyTokenToCookie(savedGuest, response);
        return savedGuest;
    }

    public User refreshUserCookie (Principal principal, HttpServletResponse response){
        Integer userId = Integer.parseInt(principal.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        jwtService.applyTokenToCookie(user, response);
        return user;
    }

    @Transactional
    public void userLogout(Integer id, HttpServletResponse response){
        roomService.handleRoomDisconnect(id);
        this.handleUserDisconnect(id);
        jwtService.removeTokenFromCookie(response);
    }

    public User loginUser(String nickname, String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user= userRepository.findByNickname(nickname).orElseThrow(()-> new BusinessException("Wrong credentials"));
        if(encoder.matches(password, user.getPassword())){
            System.out.println("User: " + user.getNickname() +" logged in successfully");
            return user;
        }else{
            throw new BusinessException("Wrong credentials");
        }
    }

    @Transactional
    public void handleUserDisconnect(Integer userId) {
        userRepository.findById(userId).ifPresent(user ->{
            if(user.getIsGuest()){
                userRepository.delete(user);
                System.out.println("Guest user: " + userId + " disconnected");
            }
        });
    }

    public User findUserByPrincipal(Principal principal) {
        return userRepository.findById(Integer.parseInt(principal.getName())).orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }
}
