package com.kvs.erasmuslink.service.service;

import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.data.model.repository.UserRepository;
import com.kvs.erasmuslink.data.model.dto.LoginDTO;
import com.kvs.erasmuslink.data.model.dto.LoginResponseDTO;
import com.kvs.erasmuslink.data.model.dto.RegisterRequestDTO;
import com.kvs.erasmuslink.data.model.dto.RegisterResponseDTO;
import com.kvs.erasmuslink.service.exception.UserException;
import com.kvs.erasmuslink.service.service.UserService;
import com.kvs.erasmuslink.service.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for handling authentication operations
 *
 * @author Stiliyan Nikolov
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;

    public LoginResponseDTO login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername());

        if (user == null) {
            throw new UserException("Invalid username or password");
        }

        boolean passwordMatches = PasswordUtil.checkPassword(loginDTO.getPassword(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new UserException("Invalid username or password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return new LoginResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getUserType().toString(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }

    /**
     * Registers a new user in the system
     */
    public RegisterResponseDTO register(RegisterRequestDTO requestDTO) {
        if (userRepository.findByUsername(requestDTO.getUsername()) != null) {
            throw new UserException(String.format("Error when creating user %s! User already exists!",
                    requestDTO.getUsername()));
        }
        
        if (userRepository.findByEmail(requestDTO.getEmail()) != null) {
            throw new UserException(String.format("Email %s is already registered!",
                    requestDTO.getEmail()));
        }
        
        if (!UserType.contains(requestDTO.getUserType())) {
            throw new UserException("Invalid user type: " + requestDTO.getUserType());
        }

        String passwordHash = PasswordUtil.hashPassword(requestDTO.getPassword());

        User user = new User(
                requestDTO.getFullName(),
                requestDTO.getUsername(),
                requestDTO.getEmail(),
                UserType.valueOf(requestDTO.getUserType().toUpperCase()),
                passwordHash
        );

        User savedUser = userRepository.save(user);

        return new RegisterResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getUserType().toString(),
                savedUser.getCreatedAt()
        );
    }
}