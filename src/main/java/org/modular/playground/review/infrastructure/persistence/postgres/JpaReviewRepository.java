package org.modular.playground.review.infrastructure.persistence.postgres;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.modular.playground.review.core.domain.ReviewStatsImpl;

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
    @SuppressWarnings("unchecked")
    public ReviewStatsImpl getReviewStats(UUID bookId) {
        LOGGER.debugf("JPA: Getting review stats for book ID: %s", bookId);
        Object[] row = (Object[]) entityManager
                .createQuery("SELECT COUNT(r), AVG(r.rating) FROM ReviewEntity r WHERE r.bookId = :bookId")
                .setParameter("bookId", bookId)
                .getSingleResult();
        Long count = (Long) row[0];
        Double avg = (Double) row[1];
        return ReviewStatsImpl.builder()
                .totalReviews(count)
                .averageRating(avg != null ? avg : 0.0)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<UUID, ReviewStatsImpl> getReviewStatsBatch(List<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) return Collections.emptyMap();
        LOGGER.debugf("JPA: Getting review stats batch for %d books", bookIds.size());
        List<Object[]> results = entityManager.createQuery(
                "SELECT r.bookId, COUNT(r), AVG(r.rating) FROM ReviewEntity r WHERE r.bookId IN :bookIds GROUP BY r.bookId")
                .setParameter("bookIds", bookIds)
                .getResultList();
        Map<UUID, ReviewStatsImpl> statsMap = new HashMap<>();
        for (Object[] row : results) {
            UUID bookId = (UUID) row[0];
            Long count = (Long) row[1];
            Double avg = (Double) row[2];
            statsMap.put(bookId, ReviewStatsImpl.builder()
                    .totalReviews(count)
                    .averageRating(avg != null ? avg : 0.0)
                    .build());
        }
        return statsMap;
    }

    @Override
    public List<Review> findByUserIdAndBookIds(UUID userId, List<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) return Collections.emptyList();
        LOGGER.debugf("JPA: Finding reviews by user %s for %d books", userId, bookIds.size());
        TypedQuery<ReviewEntity> query = entityManager.createQuery(
                "SELECT r FROM ReviewEntity r WHERE r.userId = :userId AND r.bookId IN :bookIds", ReviewEntity.class);
        query.setParameter("userId", userId);
        query.setParameter("bookIds", bookIds);
        return query.getResultList().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("JPA: Deleting all review entities");
        entityManager.createQuery("DELETE FROM ReviewEntity").executeUpdate();
    }
}
