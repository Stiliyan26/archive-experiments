package com.kvs.erasmuslink.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.erasmuslink.data.model.dto.LoginDTO;
import com.kvs.erasmuslink.data.model.dto.LoginResponseDTO;
import com.kvs.erasmuslink.data.model.dto.RegisterRequestDTO;
import com.kvs.erasmuslink.data.model.dto.RegisterResponseDTO;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.service.exception.UserException;
import com.kvs.erasmuslink.service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private ObjectMapper objectMapper;
    private LoginDTO sampleLoginDTO;
    private LoginResponseDTO sampleLoginResponseDTO;
    private RegisterRequestDTO sampleRegisterRequestDTO;
    private RegisterResponseDTO sampleRegisterResponseDTO;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        now = LocalDateTime.now();

        // Setup sample login data
        sampleLoginDTO = new LoginDTO(
                "testuser",
                "Password123"
        );

        sampleLoginResponseDTO = new LoginResponseDTO(
                1,
                "testuser",
                "Test User",
                "test@example.com",
                "PARTICIPANT",
                now.minusDays(30),
                now
        );

        // Setup sample register data
        sampleRegisterRequestDTO = new RegisterRequestDTO(
                "Test User",
                "testuser",
                "test@example.com",
                "PARTICIPANT",
                "Password123"
        );

        sampleRegisterResponseDTO = new RegisterResponseDTO(
                1,
                "testuser",
                "Test User",
                "test@example.com",
                "PARTICIPANT",
                now
        );
    }

    @Nested
    @DisplayName("POST /api/auth/login - User Login")
    class LoginTests {

        @Test
        @DisplayName("Should login user with valid credentials")
        void login_validCredentials_returnsUserData() throws Exception {
            when(authService.login(any(LoginDTO.class)))
                    .thenReturn(sampleLoginResponseDTO);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLoginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.fullName").value("Test User"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.userType").value("PARTICIPANT"));
        }

        @Test
        @DisplayName("Should return unauthorized when credentials are invalid")
        void login_invalidCredentials_returnsUnauthorized() throws Exception {
            when(authService.login(any(LoginDTO.class)))
                    .thenThrow(new UserException("Invalid username or password"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLoginDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid username or password"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when username is empty or null")
        void login_emptyUsername_returnsBadRequest(String username) throws Exception {
            sampleLoginDTO.setUsername(username);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLoginDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when username is too short")
        void login_shortUsername_returnsBadRequest() throws Exception {
            sampleLoginDTO.setUsername("ab");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLoginDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when username contains invalid characters")
        void login_invalidUsernameChars_returnsBadRequest() throws Exception {
            sampleLoginDTO.setUsername("user@name!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLoginDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when password is empty or null")
        void login_emptyPassword_returnsBadRequest(String password) throws Exception {
            sampleLoginDTO.setPassword(password);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLoginDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when password is too short")
        void login_shortPassword_returnsBadRequest() throws Exception {
            sampleLoginDTO.setPassword("pass");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleLoginDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register - User Registration")
    class RegisterTests {

        @Test
        @DisplayName("Should register user with valid input")
        void register_validInput_returnsCreatedUser() throws Exception {
            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenReturn(sampleRegisterResponseDTO);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.fullName").value("Test User"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.userType").value("PARTICIPANT"));
        }

        @Test
        @DisplayName("Should reject when username already exists")
        void register_existingUsername_returnsBadRequest() throws Exception {
            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenThrow(new UserException("Error when creating user testuser! User already exists!"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error when creating user testuser! User already exists!"));
        }

        @Test
        @DisplayName("Should reject when email already exists")
        void register_existingEmail_returnsBadRequest() throws Exception {
            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenThrow(new UserException("Email test@example.com is already registered!"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email test@example.com is already registered!"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when full name is empty or null")
        void register_emptyFullName_returnsBadRequest(String fullName) throws Exception {
            sampleRegisterRequestDTO.setFullName(fullName);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when full name is too short")
        void register_shortFullName_returnsBadRequest() throws Exception {
            sampleRegisterRequestDTO.setFullName("A");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when full name contains invalid characters")
        void register_invalidFullNameChars_returnsBadRequest() throws Exception {
            sampleRegisterRequestDTO.setFullName("Test User 123!");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when username is empty or null")
        void register_emptyUsername_returnsBadRequest(String username) throws Exception {
            sampleRegisterRequestDTO.setUsername(username);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when username is too short")
        void register_shortUsername_returnsBadRequest() throws Exception {
            sampleRegisterRequestDTO.setUsername("abc");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when username contains invalid characters")
        void register_invalidUsernameChars_returnsBadRequest() throws Exception {
            sampleRegisterRequestDTO.setUsername("user@name!");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when email is empty or null")
        void register_emptyEmail_returnsBadRequest(String email) throws Exception {
            sampleRegisterRequestDTO.setEmail(email);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when email format is invalid")
        void register_invalidEmailFormat_returnsBadRequest() throws Exception {
            sampleRegisterRequestDTO.setEmail("invalidemail");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when user type is invalid")
        void register_invalidUserType_returnsBadRequest() throws Exception {
            sampleRegisterRequestDTO.setUserType("invalid_type");

            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenThrow(new UserException("Invalid user type: invalid_type"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid user type: invalid_type"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when user type is null or empty")
        void register_nullOrEmptyUserType_returnsBadRequest(String userType) throws Exception {
            sampleRegisterRequestDTO.setUserType(userType);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when password is empty or null")
        void register_emptyPassword_returnsBadRequest(String password) throws Exception {
            sampleRegisterRequestDTO.setPassword(password);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when password is too short")
        void register_shortPassword_returnsBadRequest() throws Exception {
            sampleRegisterRequestDTO.setPassword("Pass1");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"password", "PASSWORD", "password123", "PASSWORD123"})
        @DisplayName("Should reject when password format is invalid")
        void register_invalidPasswordFormat_returnsBadRequest(String password) throws Exception {
            sampleRegisterRequestDTO.setPassword(password);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRegisterRequestDTO)))
                    .andExpect(status().isBadRequest());
        }
    }
}