package org.modular.playground.review.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.PersistenceUnit;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "jpa", enableIfMissing = false)
public class JpaReviewRepository implements ReviewRepository {

    private static final Logger LOGGER = Logger.getLogger(JpaReviewRepository.class);

    @Inject
    @PersistenceUnit("review-db")
    EntityManager entityManager;

    @Inject
    ReviewMapper mapper;

    @Override
    public Review create(Review review) {
        LOGGER.debugf("JPA: Creating review entity with ID: %s", review.getReviewId());
        ReviewEntity newEntity = mapper.toEntity(review);
        entityManager.persist(newEntity);
        return mapper.toDomain(newEntity);
    }

    @Override
    public Review update(Review review) {
        LOGGER.debugf("JPA: Updating review entity with ID: %s", review.getReviewId());
        ReviewEntity entityToUpdate = entityManager.find(ReviewEntity.class, review.getReviewId());
        if (entityToUpdate == null) {
            throw new IllegalArgumentException("Review with ID " + review.getReviewId() + " not found for update.");
        }
        mapper.updateEntityFromDomain(review, entityToUpdate);
        return mapper.toDomain(entityToUpdate);
    }

    @Override
    public Optional<Review> findById(UUID reviewId) {
        LOGGER.debugf("JPA: Finding review entity by ID: %s", reviewId);
        return Optional.ofNullable(entityManager.find(ReviewEntity.class, reviewId))
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID reviewId) {
        LOGGER.debugf("JPA: Deleting review entity with ID: %s", reviewId);
        ReviewEntity entity = entityManager.find(ReviewEntity.class, reviewId);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public List<Review> getBookReviews(UUID bookId) {
        LOGGER.debugf("JPA: Getting reviews for book ID: %s", bookId);
        TypedQuery<ReviewEntity> query = entityManager
                .createQuery("SELECT r FROM ReviewEntity r WHERE r.bookId = :bookId", ReviewEntity.class);
        query.setParameter("bookId", bookId);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Review> getUserReviews(UUID userId) {
        LOGGER.debugf("JPA: Getting reviews for user ID: %s", userId);
        TypedQuery<ReviewEntity> query = entityManager
                .createQuery("SELECT r FROM ReviewEntity r WHERE r.userId = :userId", ReviewEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId) {
        LOGGER.debugf("JPA: Finding review by user ID %s and book ID %s", userId, bookId);
        TypedQuery<ReviewEntity> query = entityManager.createQuery(
                "SELECT r FROM ReviewEntity r WHERE r.userId = :userId AND r.bookId = :bookId", ReviewEntity.class);
        query.setParameter("userId", userId);
        query.setParameter("bookId", bookId);
        return query.getResultStream()
                .map(entity -> (Review) mapper.toDomain(entity))
                .findFirst();
    }

    @Override
    public Long countReviewsByBookId(UUID bookId) {
        LOGGER.debugf("JPA: Counting reviews for book ID: %s", bookId);
        TypedQuery<Long> query = entityManager
                .createQuery("SELECT COUNT(r) FROM ReviewEntity r WHERE r.bookId = :bookId", Long.class);
        query.setParameter("bookId", bookId);
        return query.getSingleResult();
    }

    @Override
    public Double findAverageRatingByBookId(UUID bookId) {
        LOGGER.debugf("JPA: Finding average rating for book ID: %s", bookId);
        TypedQuery<Double> query = entityManager
                .createQuery("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.bookId = :bookId", Double.class);
        query.setParameter("bookId", bookId);
        return query.getSingleResult();
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("JPA: Deleting all review entities");
        entityManager.createQuery("DELETE FROM ReviewEntity").executeUpdate();
    }
}
