package org.modular.playground.review.infrastructure.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.catalog.core.usecases.BookService;
import org.modular.playground.catalog.web.dto.BookResponseDTO;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RemoteBookService implements BookService {

    private static final Logger LOGGER = Logger.getLogger(RemoteBookService.class);

    @Inject
    @RestClient
    CatalogServiceClient catalogClient;

    @Override
    public Optional<Book> getBookById(UUID bookId) {
        try {
            BookResponseDTO dto = catalogClient.getBookById(bookId);
            return Optional.of(toBook(dto));
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public List<Book> getBooksByIds(List<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<BookResponseDTO> dtos = catalogClient.getBooksByIds(bookIds);
            return dtos.stream().map(this::toBook).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.errorf(e, "Failed to fetch books from catalog-service");
            return Collections.emptyList();
        }
    }

    private Book toBook(BookResponseDTO dto) {
        return BookImpl.builder()
                .bookId(dto.getBookId())
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .authors(dto.getAuthors())
                .publicationDate(dto.getPublicationDate())
                .publisher(dto.getPublisher())
                .description(dto.getDescription())
                .pageCount(dto.getPageCount())
                .coverImageId(dto.getCoverImageId())
                .originalLanguage(dto.getOriginalLanguage())
                .genre(dto.getGenre())
                .build();
    }
}
