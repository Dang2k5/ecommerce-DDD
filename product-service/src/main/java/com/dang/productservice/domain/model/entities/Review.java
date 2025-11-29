package com.dang.productservice.domain.model.entities;

import com.dang.productservice.domain.model.aggregates.Product;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "product_reviews")
@Getter
public class Review {
    @Id
    private String reviewId;

    private String userId;
    private String userName;

    private Integer rating;
    private String comment;
    private String title;

    private LocalDateTime createdAt;
    private Boolean verifiedPurchase = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    protected Review() {
        // for JPA
    }

    public Review(String reviewId, String userId, String userName,
                  Integer rating, String comment, String title, Boolean verifiedPurchase) {
        validateInput(reviewId, userId, rating);

        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.title = title;
        this.verifiedPurchase = verifiedPurchase != null ? verifiedPurchase : false;
        this.createdAt = LocalDateTime.now();
    }

    private void validateInput(String reviewId, String userId, Integer rating) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            throw new IllegalArgumentException("Review ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    // Domain methods
    public void updateReview(Integer rating, String comment, String title) {
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            this.rating = rating;
        }
        if (comment != null) {
            this.comment = comment;
        }
        if (title != null) {
            this.title = title;
        }
    }

    // Package-private setter for bidirectional relationship
    void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(reviewId, review.reviewId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId);
    }
}