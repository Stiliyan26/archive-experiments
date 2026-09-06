package com.kvs.erasmuslink.data.model.repository;

import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.data.model.entity.Project;
import com.kvs.erasmuslink.data.model.entity.Review;
import com.kvs.erasmuslink.data.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for CRUD operations for Review
 *
 * @author Venislav Kirilov
 */
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByUserId(Integer userId);
    Optional<Review> findByUserAndProject(User user, Project project);
    Optional<Review> findByUserAndOrganization(User user, Organization organization);
}

