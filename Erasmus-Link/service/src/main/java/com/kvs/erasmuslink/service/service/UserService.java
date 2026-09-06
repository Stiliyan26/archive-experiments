package com.kvs.erasmuslink.service.service;

import com.kvs.erasmuslink.data.model.dto.UserDTO;
import com.kvs.erasmuslink.data.model.dto.update.UpdateUserDTO;
import com.kvs.erasmuslink.data.model.dto.UserResponseDTO;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.data.model.repository.UserRepository;
import com.kvs.erasmuslink.service.exception.UserException;
import com.kvs.erasmuslink.service.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for user endpoints
 *
 * @author Venislav Kirilov
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user entity
     *
     * @param userDTO contains the data for the entity
     * @return the entity
     */
    public User createUser(UserDTO userDTO) {
        validateUserType(userDTO.getUserType());
        checkUsernameUniqueness(userDTO.getUsername());
        checkEmailUniqueness(userDTO.getEmail());

        String passwordHash = PasswordUtil.hashPassword(userDTO.getPassword());

        User user = new User(
                userDTO.getFullName(),
                userDTO.getUsername(),
                userDTO.getEmail(),
                UserType.valueOf(userDTO.getUserType().toUpperCase()),
                passwordHash
        );

        return userRepository.save(user);
    }

    /**
     * Updates an existing user entity
     *
     * @param currentUsername
     * @param userDTO
     * @return
     */
    public User updateUser(String currentUsername, UpdateUserDTO userDTO) {
        User existingUser = userRepository.findByUsername(currentUsername);

        if (existingUser == null) {
            throw new UserException("User not found");
        }

        // Check if username is changing
        if (!existingUser.getUsername().equals(userDTO.getUsername())) {
            checkUsernameUniqueness(userDTO.getUsername());
            existingUser.setUsername(userDTO.getUsername());
        }

        // Check if email is changing
        if (!existingUser.getEmail().equals(userDTO.getEmail())) {
            checkEmailUniqueness(userDTO.getEmail());
            existingUser.setEmail(userDTO.getEmail());
        }

        existingUser.setFullName(userDTO.getFullName());

        return userRepository.save(existingUser);
    }

    /**
     * Gets a user by username
     *
     * @param username the username of the user
     * @return the entity
     */
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if(user == null) {
            throw new UserException(String.format("User %s not found!", username));
        }

        return user;
    }

    /**
     * Validates that user type exists in UserType
     *
     * @param userType the usertype
     */
    private void validateUserType(String userType) {
        if (!UserType.contains(userType)) {
            throw new UserException("Invalid user type: " + userType);
        }
    }

    /**
     * Ensures that the username is not already registered
     *
     * @param username the username
     */
    private void checkUsernameUniqueness(String username) {
        if (userRepository.findByUsername(username) != null) {
            throw new UserException("Username " + username + " already exists");
        }
    }

    /**
     * Ensures that the email is not already registered
     *
     * @param email the email
     */
    private void checkEmailUniqueness(String email) {
        if(userRepository.findByEmail(email) != null) {
            throw new UserException("Email " + email + " already exists!");
        }
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
