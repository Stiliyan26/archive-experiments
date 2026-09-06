package com.kvs.erasmuslink.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for login response data
 *
 * @author Stiliyan Nikolov
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String userType;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}