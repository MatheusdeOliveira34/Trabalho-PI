package com.br.app.services;

import org.springframework.stereotype.Service;

import com.br.app.entities.User;
import com.br.app.repositories.UserRepository;

import jakarta.transaction.Transactional;

import java.util.List;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Usuário não encontrado: " + id)
        );
    }

    @Transactional
    public User create(User user) {

        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("E-mail já cadastrado: " + user.getEmail());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, User user) {
        User existUser = findById(id);

        existUser.setName(user.getName());
        existUser.setEmail(user.getEmail());
        return userRepository.save(existUser);
    }

    @Transactional
    public void delete(Long id) {
      
        User existUser = findById(id);
        userRepository.delete(existUser);
        
    }
}
