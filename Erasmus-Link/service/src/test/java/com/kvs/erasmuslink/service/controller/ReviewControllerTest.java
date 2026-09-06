package com.kvs.erasmuslink.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.erasmuslink.data.model.dto.ReviewDTO;
import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.data.model.entity.Project;
import com.kvs.erasmuslink.data.model.entity.Review;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.ProjectStatus;
import com.kvs.erasmuslink.data.model.enums.ReviewTargetType;
import com.kvs.erasmuslink.data.model.enums.UserType;
import com.kvs.erasmuslink.service.service.ReviewService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    private ObjectMapper objectMapper;
    private User sampleUser;
    private Review sampleReview1;
    private Review sampleReview2;
    private Organization sampleOrganization;
    private Project sampleProject;
    private ReviewDTO sampleDTO;
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

        sampleProject = new Project(
                sampleOrganization,
                "Our New Project",
                "This project is all about learning java spring and other technologies",
                ProjectStatus.OPEN,
                LocalDate.parse("17/02/2100", formatter),
                LocalDate.parse("30/03/2100", formatter)
        );

        sampleReview1 = new Review(
                sampleUser,
                sampleProject,
                null,
                ReviewTargetType.PROJECT,
                5,
                null
        );

        sampleReview2 = new Review(
                sampleUser,
                null,
                sampleOrganization,
                ReviewTargetType.ORGANIZATION,
                4,
                "Its very good"
        );

        sampleDTO = new ReviewDTO(
                1,
                1,
                "ORGANIZATION",
                4,
                "Its very good"
        );
    }

    @Nested
    @DisplayName("POST /api/reviews - Create Review")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review for organization with valid input")
        void createReview_forOrganization_returnsCreatedReview() throws Exception {
            when(reviewService.createReview(any(ReviewDTO.class)))
                    .thenReturn(sampleReview2);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.targetType").value("ORGANIZATION"))
                    .andExpect(jsonPath("$.rating").value(4))
                    .andExpect(jsonPath("$.comment").value("Its very good"));
        }

        @Test
        @DisplayName("Should create review for project with valid input")
        void createReview_forProject_returnsCreatedReview() throws Exception {
            sampleDTO.setTargetType("PROJECT");
            when(reviewService.createReview(any(ReviewDTO.class)))
                    .thenReturn(sampleReview1);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.targetType").value("PROJECT"))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").doesNotExist());
        }

        @Test
        @DisplayName("Should reject when userId is null")
        void createReview_nullUserId_returnsBadRequest() throws Exception {
            sampleDTO.setUserId(null);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when targetId is null")
        void createReview_nullTargetId_returnsBadRequest() throws Exception {
            sampleDTO.setTargetId(null);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"INVALID", " ", "project", "organization"})
        @DisplayName("Should reject when targetType is invalid")
        void createReview_invalidTargetType_returnsBadRequest(String targetType) throws Exception {
            sampleDTO.setTargetType(targetType);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when rating is below minimum")
        void createReview_ratingBelowMin_returnsBadRequest() throws Exception {
            sampleDTO.setRating(0);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject when rating is above maximum")
        void createReview_ratingAboveMax_returnsBadRequest() throws Exception {
            sampleDTO.setRating(6);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept when comment is null")
        void createReview_nullComment_returnsSuccess() throws Exception {
            sampleDTO.setComment(null);
            when(reviewService.createReview(any(ReviewDTO.class)))
                    .thenReturn(sampleReview1);

            mockMvc.perform(post("/api/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/reviews/user/{userId} - Get Reviews by User")
    class GetReviewsByUserTests {

        @Test
        @DisplayName("Should return reviews for existing user")
        void getReviewsByUser_exists_returnsReviews() throws Exception {
            when(reviewService.getReviewsByUser(1))
                    .thenReturn(List.of(sampleReview1, sampleReview2));

            mockMvc.perform(get("/api/reviews/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].targetType").value("PROJECT"))
                    .andExpect(jsonPath("$[1].targetType").value("ORGANIZATION"))
                    .andExpect(jsonPath("$[1].comment").value("Its very good"));
        }

        @Test
        @DisplayName("Should return empty list for user with no reviews")
        void getReviewsByUser_noReviews_returnsEmptyList() throws Exception {
            when(reviewService.getReviewsByUser(2))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/reviews/user/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}

