package com.br.app.model;

import org.springframework.stereotype.Component;

import com.br.app.dto.UserRequestDTO;
import com.br.app.dto.UserResponseDTO;
import com.br.app.entities.User;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        User user = new User();

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setRole(dto.role());

        return user;
    }

    public UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getName(),
            user.getEmail()
        );
    }
    
}
