package com.example.demo.dto.user;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
