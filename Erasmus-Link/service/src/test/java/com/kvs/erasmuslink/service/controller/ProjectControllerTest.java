package com.kvs.erasmuslink.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.erasmuslink.data.model.dto.ProjectDTO;
import com.kvs.erasmuslink.data.model.dto.UserDTO;
import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.data.model.entity.Project;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.ProjectStatus;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.service.service.ProjectService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    private ObjectMapper objectMapper;
    private Organization sampleOrganization;
    private User sampleUser;
    private Project sampleProject;
    private ProjectDTO sampleDTO;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        sampleUser = new User(
                "John Doe",
                "johndoe",
                "john@example.com",
                UserType.PARTICIPANT,
                "hashedPassword"
        );

        sampleOrganization = new Organization(
                sampleUser,
                "TestOrg",
                "TestDescription",
                "Boulevard Test"
        );

        sampleDTO = new ProjectDTO(
                1,
                "Our New Project",
                "This project is all about learning java spring and other technologies",
                "OPEN",
                "17/02/2100",
                "30/03/2100"
        );

        sampleProject = new Project(
                sampleOrganization,
                "Our New Project",
                "This project is all about learning java spring and other technologies",
                ProjectStatus.OPEN,
                LocalDate.parse("17/02/2100", formatter),
                LocalDate.parse("30/03/2100", formatter)
        );
    }

    @Nested
    @DisplayName("POST /api/projects - Create Project")
    class CreateProjectTests {

        @Test
        @DisplayName("Should create project with valid input")
        void createProject_validInput_returnsCreatedProject() throws Exception {
            sampleOrganization.setId(1);

            when(projectService.createProject(any(ProjectDTO.class)))
                    .thenReturn(sampleProject);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Our New Project"))
                    .andExpect(jsonPath("$.description").value("This project is all about learning java spring and other technologies"))
                    .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when title is empty or null")
        void createProject_emptyTitle_returnsBadRequest(String title) throws Exception {
            sampleDTO.setTitle(title);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when title is too short")
        void createProject_shortTitle_returnsBadRequest() throws Exception {
            sampleDTO.setTitle("Proj");

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when title is too long")
        void createProject_longTitle_returnsBadRequest() throws Exception {
            sampleDTO.setTitle("This Project Title Is Way Too Long For The Limit");

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when title contains invalid characters")
        void createProject_invalidTitleChars_returnsBadRequest() throws Exception {
            sampleDTO.setTitle("Invalid$%Title!");

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when description is empty or null")
        void createProject_emptyDescription_returnsBadRequest(String description) throws Exception {
            sampleDTO.setDescription(description);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when description is too short")
        void createProject_shortDescription_returnsBadRequest() throws Exception {
            sampleDTO.setDescription("Too short desc");

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when description is too long")
        void createProject_longDescription_returnsBadRequest() throws Exception {
            String longDesc = "a".repeat(501);
            sampleDTO.setDescription(longDesc);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"INVALID", "open", "closed", " "})
        @DisplayName("Should reject when status is invalid")
        void createProject_invalidStatus_returnsBadRequest(String status) throws Exception {
            sampleDTO.setStatus(status);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when organizationId is null")
        void createProject_nullOrganizationId_returnsBadRequest() throws Exception {
            sampleDTO.setOrganizationId(null);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }
    }
}