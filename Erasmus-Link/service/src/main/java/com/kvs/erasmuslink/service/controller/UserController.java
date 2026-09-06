package com.kvs.erasmuslink.service.controller;

import com.kvs.erasmuslink.data.model.dto.RegisterResponseDTO;
import com.kvs.erasmuslink.data.model.dto.UserDTO;
import com.kvs.erasmuslink.data.model.dto.UserResponseDTO;
import com.kvs.erasmuslink.data.model.dto.update.UpdateUserDTO;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.service.exception.UserException;
import com.kvs.erasmuslink.service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller class for user endpoints
 *
 * @author Venislav Kirilov
 */
@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody @Valid UserDTO requestDTO) {
        return userService.createUser(requestDTO);
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> updateUser(
            @PathVariable String username,
            @RequestBody @Valid UpdateUserDTO userDTO) {
        try {
            User responseDTO = userService.updateUser(username, userDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during registration"));
        }
    }

    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
