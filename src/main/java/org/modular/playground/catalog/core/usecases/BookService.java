package org.modular.playground.catalog.core.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modular.playground.catalog.core.domain.Book;

public interface BookService {
    Optional<Book> getBookById(UUID bookId);
    List<Book> getBooksByIds(List<UUID> bookIds);
}
