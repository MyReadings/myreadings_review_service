package org.modular.playground.review.core.usecases.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modular.playground.review.core.domain.Review;

public interface ReviewRepository {
    Review create(Review review);
    Review update(Review review);
    Optional<Review> findById(UUID reviewId);
    Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId);
    void deleteById(UUID reviewId);
    List<Review> getBookReviews(UUID bookId);
    List<Review> getUserReviews(UUID userId);
    Long countReviewsByBookId(UUID bookId);
    Double findAverageRatingByBookId(UUID bookId);
    void deleteAll();
}
