package org.modular.playground.review.web.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@io.quarkus.runtime.annotations.RegisterForReflection
public class ReviewResponseDTO {
    private UUID reviewId;
    private UUID bookId;
    private UUID userId;
    private String reviewText;
    private int rating;
    private LocalDateTime publicationDate;
    private String username;
}
