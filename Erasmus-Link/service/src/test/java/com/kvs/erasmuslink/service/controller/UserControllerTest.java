package com.kvs.erasmuslink.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.erasmuslink.data.model.dto.UserDTO;
import com.kvs.erasmuslink.data.model.dto.UserResponseDTO;
import com.kvs.erasmuslink.data.model.dto.update.UpdateUserDTO;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.service.exception.UserException;
import com.kvs.erasmuslink.service.service.UserService;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private User sampleUser;
    private UserDTO sampleDTO;
    private List<UserResponseDTO> userResponseDTOs;
    private UpdateUserDTO updateUserDTO;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        sampleDTO = new UserDTO(
                "John Doe",
                "johndoe",
                "john@example.com",
                "PARTICIPANT",
                "Secret123"
        );

        sampleUser = new User(
                "John Doe",
                "johndoe",
                "john@example.com",
                UserType.PARTICIPANT,
                "hashedPassword"
        );

        // Add creation timestamp to sample user
        sampleUser.setCreatedAt(LocalDateTime.now());

        // Create test DTOs for getAllUsers tests
        User testUser2 = new User(
                "Admin User",
                "adminuser",
                "admin@example.com",
                UserType.ADMIN,
                "hashedPassword"
        );
        testUser2.setCreatedAt(LocalDateTime.now());

        UserResponseDTO dto1 = new UserResponseDTO(
                sampleUser.getUsername(),
                sampleUser.getEmail(),
                sampleUser.getFullName(),
                sampleUser.getUserType(),
                sampleUser.getCreatedAt()
        );

        UserResponseDTO dto2 = new UserResponseDTO(
                testUser2.getUsername(),
                testUser2.getEmail(),
                testUser2.getFullName(),
                testUser2.getUserType(),
                testUser2.getCreatedAt()
        );

        userResponseDTOs = Arrays.asList(dto1, dto2);

        // Setup update DTO for updateUser tests
        updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setUsername("johndoe");
        updateUserDTO.setEmail("updated@example.com");
        updateUserDTO.setFullName("Updated Name");
        updateUserDTO.setUserType("PARTICIPANT");
    }

    @Nested
    @DisplayName("POST /api/users - Create User")
    class CreateUserTests {
        @Test
        @DisplayName("Should return created user when valid request")
        void createUser_validRequest_returnsCreatedUser() throws Exception {
            when(userService.createUser(any(UserDTO.class))).thenReturn(sampleUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("johndoe"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.fullName").value("John Doe"))
                    .andExpect(jsonPath("$.userType").value("PARTICIPANT"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should return bad request when username is empty or null")
        void createUser_missingUsername_returnsBadRequest(String username) throws Exception {
            sampleDTO.setUsername(username);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"12", "", "normalUsername_.$%"})
        @DisplayName("Should return bad request when username doesn't meet requirements")
        void createUser_wrongUsername_returnBadRequest(String username) throws Exception {
            sampleDTO.setUsername(username);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID_TYPE", " ", ""})
        @NullAndEmptySource
        @DisplayName("Should return bad request when user type is invalid")
        void createUser_invalidUserType_returnsBadRequest(String userType) throws Exception {
            sampleDTO.setUserType(userType);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should return bad request when password is empty or null")
        void createUser_missingPassword_returnsBadRequest(String password) throws Exception {
            sampleDTO.setPassword(password);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"sS127", "sS4         ", "123456789"})
        @DisplayName("Should return bad request when password doesn't meet requirements")
        void createUser_wrongPassword_returnsBadRequest(String password) throws Exception {
            sampleDTO.setPassword(password);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"invalid.email", "missing@domain"})
        @DisplayName("Should return bad request when email is invalid")
        void createUser_invalidEmail_returnsBadRequest(String email) throws Exception {
            sampleDTO.setEmail(email);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should return bad request when full name is empty or null")
        void createUser_missingFullName_returnsBadRequest(String fullName) throws Exception {
            sampleDTO.setFullName(fullName);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{username} - Get User by Username")
    class GetUserTests {
        @Test
        @DisplayName("Should return user when username exists")
        void getUserByUsername_exists_returnsUser() throws Exception {
            when(userService.getUserByUsername("johndoe")).thenReturn(sampleUser);

            mockMvc.perform(get("/api/users/johndoe"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("johndoe"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.fullName").value("John Doe"))
                    .andExpect(jsonPath("$.userType").value("PARTICIPANT"));
        }
    }

    // ------------------------------------- Stiliyan Nikolov --------------------------------------------------
    @Nested
    @DisplayName("GET /api/users - Get All Users")
    class GetAllUsersTests {
        @Test
        @DisplayName("Should return all users when requested")
        void getAllUsers_returnsUserList() throws Exception {
            // Setup mock service
            when(userService.getAllUsers()).thenReturn(userResponseDTOs);

            // Execute the GET request and validate the response
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].username").value("johndoe"))
                    .andExpect(jsonPath("$[0].email").value("john@example.com"))
                    .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                    .andExpect(jsonPath("$[0].userType").value("PARTICIPANT"))
                    .andExpect(jsonPath("$[1].username").value("adminuser"))
                    .andExpect(jsonPath("$[1].userType").value("ADMIN"));
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAllUsers_noUsers_returnsEmptyList() throws Exception {
            // Setup mock service to return empty list
            when(userService.getAllUsers()).thenReturn(List.of());

            // Execute the GET request and validate the response
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{username} - Update User")
    class UpdateUserTests {
        @Test
        @DisplayName("Should update user when valid request")
        void updateUser_validRequest_returnsUpdatedUser() throws Exception {
            // Create updated user response
            User updatedUser = new User(
                    updateUserDTO.getFullName(),
                    updateUserDTO.getUsername(),
                    updateUserDTO.getEmail(),
                    UserType.PARTICIPANT,
                    "hashedPassword"
            );
            updatedUser.setCreatedAt(LocalDateTime.now());

            // Setup mock service
            when(userService.updateUser(eq("johndoe"), any(UpdateUserDTO.class))).thenReturn(updatedUser);

            // Execute the PUT request and validate the response
            mockMvc.perform(put("/api/users/johndoe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value("johndoe"))
                    .andExpect(jsonPath("$.email").value("updated@example.com"))
                    .andExpect(jsonPath("$.fullName").value("Updated Name"))
                    .andExpect(jsonPath("$.userType").value("PARTICIPANT"));
        }

        @Test
        @DisplayName("Should return 400 when user not found")
        void updateUser_userNotFound_returnsBadRequest() throws Exception {
            // Setup mock service to throw exception
            when(userService.updateUser(eq("nonexistent"), any(UpdateUserDTO.class)))
                    .thenThrow(new UserException("User not found"));

            // Execute the PUT request and validate the response
            mockMvc.perform(put("/api/users/nonexistent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when updating with existing email")
        void updateUser_duplicateEmail_returnsBadRequest() throws Exception {
            // Setup mock service to throw exception
            when(userService.updateUser(eq("johndoe"), any(UpdateUserDTO.class)))
                    .thenThrow(new UserException("Email updated@example.com already exists!"));

            // Execute the PUT request and validate the response
            mockMvc.perform(put("/api/users/johndoe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should return 400 when username is empty or null")
        void updateUser_invalidUsername_returnsBadRequest(String username) throws Exception {
            // Create invalid DTO
            UpdateUserDTO invalidDTO = new UpdateUserDTO();
            invalidDTO.setUsername(username);
            invalidDTO.setEmail("valid@example.com");
            invalidDTO.setFullName("Valid Name");
            invalidDTO.setUserType("PARTICIPANT");

            // Execute the PUT request and validate the response
            mockMvc.perform(put("/api/users/johndoe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid.email", "missing@domain"})
        @NullAndEmptySource
        @DisplayName("Should return 400 when email is invalid")
        void updateUser_invalidEmail_returnsBadRequest(String email) throws Exception {
            // Create invalid DTO
            UpdateUserDTO invalidDTO = new UpdateUserDTO();
            invalidDTO.setUsername("johndoe");
            invalidDTO.setEmail(email);
            invalidDTO.setFullName("Valid Name");
            invalidDTO.setUserType("PARTICIPANT");

            // Execute the PUT request and validate the response
            mockMvc.perform(put("/api/users/johndoe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should update user type when valid")
        void updateUser_changeUserType_returnsUpdatedUser() throws Exception {
            // Create DTO with different user type
            UpdateUserDTO typeChangeDTO = new UpdateUserDTO();
            typeChangeDTO.setUsername("johndoe");
            typeChangeDTO.setEmail("john@example.com");
            typeChangeDTO.setFullName("John Doe");
            typeChangeDTO.setUserType("ORGANIZATION");

            // Create expected updated user
            User updatedUser = new User(
                    "John Doe",
                    "johndoe",
                    "john@example.com",
                    UserType.ORGANIZATION, // Changed type
                    "hashedPassword"
            );
            updatedUser.setCreatedAt(LocalDateTime.now());

            // Setup mock service
            when(userService.updateUser(eq("johndoe"), any(UpdateUserDTO.class))).thenReturn(updatedUser);

            // Execute the PUT request and validate the response
            mockMvc.perform(put("/api/users/johndoe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(typeChangeDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userType").value("ORGANIZATION"));
        }
    }
}