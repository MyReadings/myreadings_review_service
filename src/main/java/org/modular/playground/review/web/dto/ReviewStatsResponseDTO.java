package org.modular.playground.review.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewStatsResponseDTO {
    private String bookId;
    private long totalReviews;
    private Double averageRating;
}
