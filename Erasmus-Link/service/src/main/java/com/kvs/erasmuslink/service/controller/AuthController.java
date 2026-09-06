package com.kvs.erasmuslink.service.controller;

import com.kvs.erasmuslink.data.model.dto.LoginDTO;
import com.kvs.erasmuslink.data.model.dto.LoginResponseDTO;
import com.kvs.erasmuslink.data.model.dto.RegisterRequestDTO;
import com.kvs.erasmuslink.data.model.dto.RegisterResponseDTO;
import com.kvs.erasmuslink.service.exception.UserException;
import com.kvs.erasmuslink.service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

/**
 * Controller handling authentication operations
 *
 * @author Stiliyan Nikolov
 */
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO loginDTO) {
        try {
            LoginResponseDTO responseDTO = authService.login(loginDTO);

            return ResponseEntity.ok(responseDTO);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during login"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTO requestDTO) {
        try {
            RegisterResponseDTO responseDTO = authService.register(requestDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during registration"));
        }
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