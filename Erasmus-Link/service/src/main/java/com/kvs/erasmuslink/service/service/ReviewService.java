package com.kvs.erasmuslink.service.service;

import com.kvs.erasmuslink.data.model.dto.ReviewDTO;
import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.data.model.entity.Project;
import com.kvs.erasmuslink.data.model.entity.Review;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.enums.ReviewTargetType;
import com.kvs.erasmuslink.data.model.repository.OrganizationRepository;
import com.kvs.erasmuslink.data.model.repository.ProjectRepository;
import com.kvs.erasmuslink.data.model.repository.ReviewRepository;
import com.kvs.erasmuslink.data.model.repository.UserRepository;
import com.kvs.erasmuslink.service.exception.ReviewException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for review endpoints
 *
 * @author Venislav Kirilov
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            ProjectRepository projectRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Creates a new review entity
     *
     * @param reviewDTO contains the data for the entity
     * @return the entity
     */
    public Review createReview(ReviewDTO reviewDTO) {
        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ReviewException("User not found!"));

        ReviewTargetType targetType = ReviewTargetType.valueOf(reviewDTO.getTargetType().toUpperCase());

        if (targetType == ReviewTargetType.PROJECT) {
            Project project = projectRepository.findById(reviewDTO.getTargetId())
                    .orElseThrow(() -> new ReviewException("Project not found!"));

            reviewRepository.findByUserAndProject(user, project)
                    .ifPresent(existing -> {
                        throw new ReviewException("You have already reviewed this project!");
                    });

            Review review = new Review(
                    user,
                    project,
                    null,
                    targetType,
                    reviewDTO.getRating(),
                    reviewDTO.getComment() == null ? "" : reviewDTO.getComment()
            );

            return reviewRepository.save(review);

        } else if (targetType == ReviewTargetType.ORGANIZATION) {
            Organization organization = organizationRepository.findById(reviewDTO.getTargetId())
                    .orElseThrow(() -> new ReviewException("Organization not found!"));

            reviewRepository.findByUserAndOrganization(user, organization)
                    .ifPresent(existing -> {
                        throw new ReviewException("You have already reviewed this organization!");
                    });

            Review review = new Review(
                    user,
                    null,
                    organization,
                    targetType,
                    reviewDTO.getRating(),
                    reviewDTO.getComment() == null ? "" : reviewDTO.getComment()
            );

            return reviewRepository.save(review);
        }

        throw new ReviewException("Invalid target type!");
    }

    /**
     * Gets a list of the reviews of a specific user
     *
     * @param userId the user's id
     * @return list of the user's reviews
     */
    public List<Review> getReviewsByUser(Integer userId) {
        return  reviewRepository.findByUserId(userId);
    }
}
