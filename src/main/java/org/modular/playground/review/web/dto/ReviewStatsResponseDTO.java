package org.modular.playground.review.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@io.quarkus.runtime.annotations.RegisterForReflection
public class ReviewStatsResponseDTO {
    private String bookId;
    private long totalReviews;
    private Double averageRating;
}
