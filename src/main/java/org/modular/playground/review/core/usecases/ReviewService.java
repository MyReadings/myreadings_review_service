package org.modular.playground.review.core.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewStatsImpl;
import org.modular.playground.review.web.dto.ReviewRequestDTO;

public interface ReviewService {
    Review createReview(ReviewRequestDTO reviewRequest, JsonWebToken principal);
    Optional<Review> findReviewById(UUID reviewId, JsonWebToken principal);
    Review findReviewAndCheckOwnership(UUID reviewId, JsonWebToken principal);
    Optional<Review> findReviewByUserAndBook(UUID userId, UUID bookId, JsonWebToken principal);
    List<Review> getReviewsForBook(UUID bookId, JsonWebToken principal);
    List<Review> getReviewsForUser(UUID userId, JsonWebToken principal);
    Review updateReview(UUID reviewId, ReviewRequestDTO reviewRequest, JsonWebToken principal); 
    void deleteReviewById(UUID reviewId, JsonWebToken principal);
    ReviewStatsImpl getReviewStatsForBook(UUID bookId);
}
