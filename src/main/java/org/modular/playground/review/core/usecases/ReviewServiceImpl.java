package org.modular.playground.review.core.usecases;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.common.security.SecurityUtils;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewImpl;
import org.modular.playground.review.core.domain.ReviewStatsImpl;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;
import org.modular.playground.review.web.dto.ReviewRequestDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.UserService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = Logger.getLogger(ReviewServiceImpl.class);

    @Inject
    ReviewRepository reviewRepository;
    @Inject
    BookService bookService;
    @Inject
    UserService userService;
    @Inject
    ReviewMapper reviewMapper;

    @Override
    public Review createReview(ReviewRequestDTO reviewRequest, JsonWebToken principal) {
        UUID userId = UUID.fromString(principal.getSubject());
        UUID bookId = reviewRequest.getBookId();
        LOGGER.infof("Attempting to create review for book %s by user %s", bookId, userId);

        Book book = bookService.getBookById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found: " + bookId));
        User user = userService.findUserProfileById(userId, principal)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Review reviewToCreate = reviewMapper.toDomain(reviewRequest, user, book);

        return createInTransaction(reviewToCreate);
    }

    @Override
    public Review findReviewAndCheckOwnership(UUID reviewId, JsonWebToken principal) {
        LOGGER.debugf("Finding review %s and checking ownership for user %s", reviewId, principal.getSubject());
        Review review = findByIdInTransaction(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with ID: " + reviewId));

        checkOwnership(review, principal);
        return enrichReview(review);
    }

    @Override
    public Optional<Review> findReviewById(UUID reviewId, JsonWebToken principal) {
        LOGGER.debugf("Searching for review by ID: %s", reviewId);
        return findByIdInTransaction(reviewId).map(this::enrichReview);
    }

    @Override
    public List<Review> getReviewsForBook(UUID bookId, JsonWebToken principal) {
        LOGGER.debugf("Getting reviews for book ID: %s", bookId);
        List<Review> reviews = getBookReviewsInTransaction(bookId);
        return enrichReviews(reviews);
    }

    @Override
    public List<Review> getReviewsForUser(UUID userId, JsonWebToken principal) {
        LOGGER.debugf("Getting reviews for user ID: %s", userId);
        
        if (userService.findUserProfileById(userId, principal).isEmpty()) {
            LOGGER.warnf("User not found: %s. Cannot retrieve reviews.", userId);
            return Collections.emptyList();
        }

        List<Review> reviews = getUserReviewsInTransaction(userId);
        return enrichReviews(reviews);
    }

    @Override
    public Review updateReview(UUID reviewId, ReviewRequestDTO reviewRequest, JsonWebToken principal) {
        LOGGER.infof("Attempting to update review with ID: %s", reviewId);
        Review existingReview = findReviewAndCheckOwnership(reviewId, principal);
        reviewMapper.updateFromDto(reviewRequest, (ReviewImpl) existingReview);
        return updateInTransaction(existingReview);
    }

    @Override
    public void deleteReviewById(UUID reviewId, JsonWebToken principal) {
        LOGGER.infof("Attempting to delete review with ID: %s", reviewId);
        Review reviewToDelete = findReviewAndCheckOwnership(reviewId, principal);
        deleteByIdInTransaction(reviewToDelete.getReviewId());
    }

    @Override
    public Optional<Review> findReviewByUserAndBook(UUID userId, UUID bookId, JsonWebToken principal) {
        LOGGER.debugf("Searching for review by user ID %s and book ID %s", userId, bookId);
        Optional<Review> reviewOpt = findByUserIdAndBookIdInTransaction(userId, bookId);
        return reviewOpt.map(this::enrichReview);
    }

    @Override
    public ReviewStatsImpl getReviewStatsForBook(UUID bookId) {
        LOGGER.debugf("Getting review stats for book ID: %s", bookId);
        if (bookService.getBookById(bookId).isEmpty()) {
            throw new NotFoundException("Book not found with ID: " + bookId);
        }
        return getReviewStatsInTransaction(bookId);
    }

    private void checkOwnership(Review review, JsonWebToken principal) {
        UUID callerId = UUID.fromString(principal.getSubject());
        if (!review.getUser().getKeycloakUserId().equals(callerId) && !SecurityUtils.isAdmin(principal)) {
            throw new ForbiddenException("You are not authorized to access this review.");
        }
    }

    private Review enrichReview(Review review) {
        if (review == null)
            return null;
        User fullUser = userService.findUserByIdInternal(review.getUser().getKeycloakUserId()).orElse(review.getUser());
        Book fullBook = bookService.getBookById(review.getBook().getBookId()).orElse(review.getBook());

        ((ReviewImpl) review).setUser(fullUser);
        ((ReviewImpl) review).setBook(fullBook);
        return review;
    }

    private List<Review> enrichReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> userIds = reviews.stream().map(r -> r.getUser().getKeycloakUserId()).distinct()
                .collect(Collectors.toList());
        List<UUID> bookIds = reviews.stream().map(r -> r.getBook().getBookId()).distinct().collect(Collectors.toList());

        Map<UUID, User> usersMap = userService.findUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getKeycloakUserId, Function.identity()));
        Map<UUID, Book> booksMap = bookService.getBooksByIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getBookId, Function.identity()));

        reviews.forEach(review -> {
            ReviewImpl mutableReview = (ReviewImpl) review;
            mutableReview.setUser(usersMap.get(review.getUser().getKeycloakUserId()));
            mutableReview.setBook(booksMap.get(review.getBook().getBookId()));
        });

        return reviews;
    }

    @Transactional
    protected Review createInTransaction(Review review) {
        return reviewRepository.create(review);
    }

    @Transactional
    protected Review updateInTransaction(Review review) {
        return reviewRepository.update(review);
    }

    @Transactional
    protected Optional<Review> findByIdInTransaction(UUID reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Transactional
    protected List<Review> getBookReviewsInTransaction(UUID bookId) {
        return reviewRepository.getBookReviews(bookId);
    }

    @Transactional
    protected List<Review> getUserReviewsInTransaction(UUID userId) {
        return reviewRepository.getUserReviews(userId);
    }

    @Transactional
    protected void deleteByIdInTransaction(UUID reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Transactional
    protected Optional<Review> findByUserIdAndBookIdInTransaction(UUID userId, UUID bookId) {
        return reviewRepository.findByUserIdAndBookId(userId, bookId);
    }

    @Transactional
    protected ReviewStatsImpl getReviewStatsInTransaction(UUID bookId) {
        Long totalReviews = reviewRepository.countReviewsByBookId(bookId);
        Double averageRating = reviewRepository.findAverageRatingByBookId(bookId);
        return ReviewStatsImpl.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating == null ? 0.0 : averageRating)
                .build();
    }
}
