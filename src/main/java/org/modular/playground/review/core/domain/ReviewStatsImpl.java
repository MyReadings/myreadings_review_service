package org.modular.playground.review.core.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@io.quarkus.runtime.annotations.RegisterForReflection
public class ReviewStatsImpl {
    private long totalReviews;
    private Double averageRating;
}
