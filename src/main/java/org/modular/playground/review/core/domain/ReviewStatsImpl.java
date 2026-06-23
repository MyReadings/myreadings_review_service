package org.modular.playground.review.core.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewStatsImpl {
    private long totalReviews;
    private Double averageRating;
}
