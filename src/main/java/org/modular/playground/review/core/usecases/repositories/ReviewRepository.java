package org.modular.playground.review.core.usecases.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewStatsImpl;

public interface ReviewRepository {
    Review create(Review review);
    Review update(Review review);
    Optional<Review> findById(UUID reviewId);
    Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId);
    void deleteById(UUID reviewId);
    List<Review> getBookReviews(UUID bookId);
    List<Review> getUserReviews(UUID userId);
    ReviewStatsImpl getReviewStats(UUID bookId);
    Map<UUID, ReviewStatsImpl> getReviewStatsBatch(List<UUID> bookIds);
    List<Review> findByUserIdAndBookIds(UUID userId, List<UUID> bookIds);
    void deleteAll();
}
