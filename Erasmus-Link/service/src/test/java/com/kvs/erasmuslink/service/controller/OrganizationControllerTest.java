package com.kvs.erasmuslink.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.erasmuslink.data.model.dto.OrganizationDTO;
import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.service.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganizationService organizationService;

    private ObjectMapper objectMapper;
    private OrganizationDTO sampleDTO;
    private Organization sampleOrganization;
    private User sampleUser;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        sampleDTO = new OrganizationDTO(
                1,
                "TestOrg",
                "TestDescription",
                "Boulevard Test"
        );

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
    }

    @Nested
    @DisplayName("POST /api/organizations - Create Organization")
    class CreateOrganizationTest {

        @Test
        @DisplayName("Should create organization with valid input")
        void createOrganization_validInput_returnsCreatedOrganization() throws Exception {
            sampleUser.setId(1);

            when(organizationService.createOrganization(any(OrganizationDTO.class)))
                    .thenReturn(sampleOrganization);

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("TestOrg"))
                    .andExpect(jsonPath("$.manager.id").value(1))
                    .andExpect(jsonPath("$.description").value("TestDescription"))
                    .andExpect(jsonPath("$.contactInfo").value("Boulevard Test"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when name is empty or null")
        void createOrganization_emptyName_returnsBadRequest(String name) throws Exception {
            sampleDTO.setName(name);

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when name is too short")
        void createOrganization_shortName_returnsBadRequest() throws Exception {
            sampleDTO.setName("Org");

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when name is too long")
        void createOrganization_longName_returnsBadRequest() throws Exception {
            sampleDTO.setName("ThisOrganizationNameIsWayTooLongForTheLimit");

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when name contains invalid characters")
        void createOrganization_invalidNameChars_returnsBadRequest() throws Exception {
            sampleDTO.setName("Invalid Org Name!");

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when managerId is null")
        void createOrganization_nullManagerId_returnsBadRequest() throws Exception {
            sampleDTO.setManagerId(null);

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject when contactInfo is empty or null")
        void createOrganization_emptyContactInfo_returnsBadRequest(String contactInfo) throws Exception {
            sampleDTO.setContactInfo(contactInfo);

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when contactInfo is too short")
        void createOrganization_shortContactInfo_returnsBadRequest() throws Exception {
            sampleDTO.setContactInfo("123");

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when contactInfo is too long")
        void createOrganization_longContactInfo_returnsBadRequest() throws Exception {
            sampleDTO.setContactInfo("This contact info is way too long and exceeds the maximum allowed length");

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept when description is null")
        void createOrganization_nullDescription_returnsSuccess() throws Exception {
            sampleDTO.setDescription(null);

            when(organizationService.createOrganization(any(OrganizationDTO.class)))
                    .thenReturn(sampleOrganization);

            mockMvc.perform(post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk());
        }
    }
}