package org.modular.playground.review.core.usecases.repositories;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(Throwable cause) {
        super("A review already exists for this user and book", cause);
    }
}
