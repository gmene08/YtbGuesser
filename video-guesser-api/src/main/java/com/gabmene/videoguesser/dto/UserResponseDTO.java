package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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