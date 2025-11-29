package com.dang.productservice.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
public class ProductStatistics {
    private Integer totalReviews = 0;
    private Double averageRating = 0.0;
    private int reviewCount = 0;
    private Integer totalSold = 0;
    private Integer viewCount = 0;

    public void updateRating(Integer newRating) {
        this.totalReviews++;
        this.averageRating = ((this.averageRating * (totalReviews - 1)) + newRating) / totalReviews;
        this.averageRating = BigDecimal.valueOf(this.averageRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public void removeRating(Integer ratingToRemove) {
        if (totalReviews > 0) {
            this.totalReviews--;
            if (totalReviews == 0) {
                this.averageRating = 0.0;
            } else {
                this.averageRating = ((this.averageRating * (totalReviews + 1)) - ratingToRemove) / totalReviews;
                this.averageRating = BigDecimal.valueOf(this.averageRating)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }
        }
    }

    public void incrementSold() {
        this.totalSold++;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }


    public void addReview(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        this.totalReviews += rating;
        this.reviewCount++;
        this.averageRating = (double) totalReviews/ reviewCount;
    }

    // ✅ THÊM METHOD removeReview()
    public void removeReview(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (reviewCount > 0) {
            this.totalReviews-= rating;
            this.reviewCount--;

            if (reviewCount > 0) {
                this.averageRating = (double) totalReviews / reviewCount;
            } else {
                this.averageRating = 0.0;
                this.totalReviews= 0;
            }
        }
    }

    public void updateRating(int rating) {
        addReview(rating);
    }

}