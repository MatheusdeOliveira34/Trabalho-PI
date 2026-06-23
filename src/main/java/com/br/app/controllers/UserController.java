package com.br.app.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.app.dto.UserRequestDTO;
import com.br.app.dto.UserResponseDTO;
import com.br.app.entities.User;
import com.br.app.model.UserMapper;
import com.br.app.services.UserService;



@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(
        UserService userService,
        UserMapper userMapper
    ) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/readAll")
    public ResponseEntity<List<UserResponseDTO>> findAll() {

        List<UserResponseDTO> users = userService.findAll()
            .stream()
            .map(user -> userMapper.toDTO(user))
            .toList();

        return ResponseEntity.status(HttpStatus.OK).body(users);
    }
 
    @GetMapping("/readById/{userId}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long userId) {
        User user = userService.findById(userId);

        return ResponseEntity.status(HttpStatus.OK).body(userMapper.toDTO(user));
    }
    
    @PostMapping("/create")
    public ResponseEntity<UserResponseDTO> requestMethodName(@RequestBody UserRequestDTO userObject) {

        User user = userMapper.toEntity(userObject);

        User createdUser = userService.create(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(createdUser));
    }

    @PostMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUser(
        @RequestParam Long userId,
        @RequestBody UserRequestDTO userObject
    ) {

        User userRequest = userMapper.toEntity(userObject);

        User userResponse = userService.update(userId, userRequest);

        return ResponseEntity.status(HttpStatus.OK).body(userMapper.toDTO(userResponse));
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
