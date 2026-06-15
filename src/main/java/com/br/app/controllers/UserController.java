package com.br.app.controllers;

import java.util.List;

import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties.Apiversion.Use;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.app.entities.User;
import com.br.app.services.UserService;



@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/readAll")
    public List<User> findAll() {
        return userService.findAll();
    }
 
    @GetMapping("/readById{userId}")
    public User findById(@RequestParam Long userId) {
        return userService.findById(userId);
    }
    
    
    @GetMapping("/create")
    public ResponseEntity<User> requestMethodName(@RequestParam User userObject) {
        User createdUser = userService.create(userObject);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/update")
    public User updateUser(@RequestParam Long userId, @RequestParam User userObject) {
        return userService.update(userId, userObject);
    }

    @GetMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
