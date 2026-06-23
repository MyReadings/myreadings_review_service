package org.modular.playground.review.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.user.core.domain.User;

public interface Review {
    UUID getReviewId();
    Book getBook();
    User getUser();
    String getReviewText();
    int getRating();
    LocalDateTime getPublicationDate();
}
