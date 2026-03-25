package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private Integer id;
    private String nickname;
    private String email;
    private Boolean isGuest;


    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.isGuest = user.getIsGuest();
    }
}