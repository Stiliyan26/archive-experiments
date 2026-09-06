package com.kvs.erasmuslink.service.controller;

import com.kvs.erasmuslink.data.model.dto.ReviewDTO;
import com.kvs.erasmuslink.data.model.entity.Review;
import com.kvs.erasmuslink.service.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for review endpoints
 *
 * @author Venislav Kirilov
 */
@RestController
@RequestMapping("api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@RequestBody @Valid ReviewDTO requestDTO) {
        return reviewService.createReview(requestDTO);
    }

    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable Integer userId) {
        return reviewService.getReviewsByUser(userId);
    }
}

