package com.kvs.erasmuslink.data.model.dto;

import jakarta.validation.constraints.*;

/**
 * DTO class for Review Entity
 *
 * @author Venislav Kirilov
 */
public class ReviewDTO {
    @NotNull(message = "UserId cannot be null")
    private Integer userId;

    @NotNull(message = "TargetId cannot be null")
    private Integer targetId;

    @NotBlank(message = "TargetType cannot be blank")
    @Pattern(regexp = "ORGANIZATION|PROJECT",
            message = "TargetType can be either ORGANIZATION or PROJECT")
    private String targetType;

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    private String comment;

    public ReviewDTO() {}

    public ReviewDTO(Integer userId, Integer targetId, String targetType, int rating, String comment) {
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.rating = rating;
        this.comment = comment;
    }

    public Integer getUserId() { return userId; }

    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getTargetId() { return targetId; }

    public void setTargetId(Integer targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }

    public void setTargetType(String targetType) { this.targetType = targetType; }

    public int getRating() { return rating; }

    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }
}

