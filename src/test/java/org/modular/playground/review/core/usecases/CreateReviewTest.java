package org.modular.playground.review.core.usecases;

import jakarta.ws.rs.ClientErrorException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.review.core.domain.ReviewImpl;
import org.modular.playground.review.core.usecases.repositories.ReviewRepository;
import org.modular.playground.review.core.usecases.repositories.DuplicateReviewException;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;
import org.modular.playground.review.web.dto.ReviewRequestDTO;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.core.usecases.UserService;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateReviewTest {
    @Mock ReviewRepository repository;
    @Mock BookService books;
    @Mock UserService users;
    @Mock ReviewMapper mapper;
    @Mock JsonWebToken principal;
    @InjectMocks ReviewServiceImpl service;

    @Test
    void existingReviewIsConflictBeforeRemoteCalls() {
        var userId = UUID.randomUUID();
        var bookId = UUID.randomUUID();
        when(principal.getSubject()).thenReturn(userId.toString());
        when(repository.findByUserIdAndBookId(userId, bookId)).thenReturn(Optional.of(ReviewImpl.builder().build()));
        var error = assertThrows(ClientErrorException.class, () -> service.createReview(
                new ReviewRequestDTO(bookId, 4, "Review"), principal));
        assertEquals(409, error.getResponse().getStatus());
        verifyNoInteractions(books, users, mapper);
        verify(repository, never()).create(any());
    }

    @Test
    void concurrentDuplicateIsConflictAndOtherFailuresStillPropagate() {
        var userId = UUID.randomUUID();
        var bookId = UUID.randomUUID();
        var request = new ReviewRequestDTO(bookId, 4, "Review");
        var user = UserImpl.builder().keycloakUserId(userId).build();
        var book = BookImpl.builder().bookId(bookId).build();
        var review = ReviewImpl.builder().build();
        when(principal.getSubject()).thenReturn(userId.toString());
        when(repository.findByUserIdAndBookId(userId, bookId)).thenReturn(Optional.empty());
        when(books.getBookById(bookId)).thenReturn(Optional.of(book));
        when(users.findUserProfileById(userId, principal)).thenReturn(Optional.of(user));
        when(mapper.toDomain(request, user, book)).thenReturn(review);
        when(repository.create(review)).thenThrow(new DuplicateReviewException(null));
        assertEquals(409, assertThrows(ClientErrorException.class,
                () -> service.createReview(request, principal)).getResponse().getStatus());
        var outage = new IllegalStateException("Database unavailable");
        doThrow(outage).when(repository).create(review);
        assertSame(outage, assertThrows(IllegalStateException.class, () -> service.createReview(request, principal)));
    }
}
