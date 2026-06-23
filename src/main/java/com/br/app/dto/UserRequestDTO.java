package com.br.app.dto;

public record UserRequestDTO(
    String name,
    String email,
    String password,
    String role
) {}