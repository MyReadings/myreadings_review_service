package org.modular.playground.review.infrastructure.persistence.postgres;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.review.core.domain.ReviewImpl;
import org.modular.playground.review.core.usecases.repositories.DuplicateReviewException;
import org.modular.playground.user.core.domain.UserImpl;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

// Explicit opt-in only: the supplied disposable database schema is recreated.
@QuarkusTest
@TestProfile(JpaReviewIntegrityTest.DatabaseProfile.class)
@EnabledIfEnvironmentVariable(named = "REVIEW_TEST_DB_URL", matches = ".+")
public class JpaReviewIntegrityTest {
    public static class DatabaseProfile implements QuarkusTestProfile {
        public Map<String, String> getConfigOverrides() {
            return Map.ofEntries(
                Map.entry("quarkus.devservices.enabled", "false"),
                Map.entry("quarkus.oidc.enabled", "false"),
                Map.entry("quarkus.otel.sdk.disabled", "true"),
                Map.entry("quarkus.http.test-port", "0"),
                Map.entry("quarkus.datasource.\"review-db\".username", "postgres"),
                Map.entry("quarkus.datasource.\"review-db\".password", "demo-test-only"),
                Map.entry("quarkus.datasource.\"review-db\".jdbc.url", System.getenv().getOrDefault("REVIEW_TEST_DB_URL", "jdbc:postgresql://localhost:1/review_test")),
                Map.entry("quarkus.hibernate-orm.\"review-db\".database.generation", "drop-and-create"));
        }
    }
    @Inject JpaReviewRepository repository;
    @Inject @PersistenceUnit("review-db") EntityManager entityManager;

    @BeforeEach
    void clean() { QuarkusTransaction.requiringNew().run(() -> entityManager.createQuery("DELETE FROM ReviewEntity").executeUpdate()); }

    private void create(UUID user, UUID book) {
        QuarkusTransaction.requiringNew().run(() -> repository.create(ReviewImpl.builder()
            .user(UserImpl.builder().keycloakUserId(user).build())
            .book(BookImpl.builder().bookId(book).build())
            .rating(4).reviewText("Review").publicationDate(LocalDateTime.now()).build()));
    }

    @Test
    void repeatedCreateRejectsDuplicateButAllowsOtherUsersAndBooks() {
        var user = UUID.randomUUID();
        var book = UUID.randomUUID();
        create(user, book);
        assertThrows(DuplicateReviewException.class, () -> create(user, book));
        create(UUID.randomUUID(), book);
        create(user, UUID.randomUUID());
        assertEquals(3L, count());
    }

    @Test
    void concurrentCreatesLeaveExactlyOneReview() throws Exception {
        var user = UUID.randomUUID();
        var book = UUID.randomUUID();
        var start = new CountDownLatch(1);
        var workers = Executors.newFixedThreadPool(2);
        Callable<Boolean> attempt = () -> {
            start.await();
            try { create(user, book); return true; }
            catch (DuplicateReviewException expected) { return false; }
        };
        try {
            var first = workers.submit(attempt);
            var second = workers.submit(attempt);
            start.countDown();
            assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        } finally { workers.shutdownNow(); }
        assertEquals(1L, count());
    }

    @Test
    void reviewPagesAreBoundedStableAndDoNotOverlap() {
        var user = UUID.randomUUID();
        for (int i = 0; i < 5; i++) create(user, UUID.randomUUID());
        QuarkusTransaction.requiringNew().run(() -> {
            var first = repository.getUserReviews(user, 0, 2);
            var second = repository.getUserReviews(user, 2, 2);
            var last = repository.getUserReviews(user, 4, 2);
            assertEquals(2, first.size());
            assertEquals(2, second.size());
            assertEquals(1, last.size());
            var ids = java.util.stream.Stream.of(first, second, last).flatMap(java.util.List::stream)
                    .map(r -> r.getReviewId()).toList();
            assertEquals(5, new java.util.HashSet<>(ids).size());
            assertEquals(ids.subList(0, 2), repository.getUserReviews(user, 0, 2).stream().map(r -> r.getReviewId()).toList());
            assertTrue(first.get(0).getPublicationDate().compareTo(last.get(0).getPublicationDate()) >= 0);
            assertTrue(repository.getUserReviews(user, 5, 2).isEmpty());
            assertEquals(1, repository.getBookReviews(first.get(0).getBook().getBookId(), 0, 2).size());
        });
    }

    private long count() {
        return QuarkusTransaction.requiringNew().call(() -> entityManager.createQuery("SELECT COUNT(r) FROM ReviewEntity r", Long.class).getSingleResult());
    }
}
