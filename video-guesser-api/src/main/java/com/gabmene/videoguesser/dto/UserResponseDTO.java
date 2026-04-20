package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String nickname;
    private String email;
    private Boolean isGuest;

    public static UserResponseDTO from(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getIsGuest());
    }

}