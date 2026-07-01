package org.modular.playground.review.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@io.quarkus.runtime.annotations.RegisterForReflection
public class ReviewRequestDTO {
    @NotNull(message = "Book ID is mandatory")
    private UUID bookId;

    @NotNull(message = "Rating is mandatory")
    @Min(1)
    @Max(5)
    private int rating;

    private String reviewText;
}
