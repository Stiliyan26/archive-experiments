package com.kvs.erasmuslink.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user registration response data
 *
 * @author Your Name
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDTO {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String userType;
    private LocalDateTime createdAt;
}