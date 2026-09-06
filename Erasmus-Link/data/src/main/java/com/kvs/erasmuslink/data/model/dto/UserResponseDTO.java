package com.kvs.erasmuslink.data.model.dto;

import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning user information
 *
 * @author Stiliyan Nikolov
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String username;
    private String email;
    private String fullName;
    private UserType userType;
    private LocalDateTime createdAt;

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getUserType(),
                user.getCreatedAt()
        );
    }
}