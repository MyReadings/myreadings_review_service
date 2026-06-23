package org.modular.playground.catalog.core.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


public interface Book {

    UUID getBookId();

    String getIsbn();

    String getTitle();

    List<String> getAuthors();

    LocalDate getPublicationDate();

    String getPublisher();

    String getDescription();

    int getPageCount();

    String getCoverImageId();

    String getOriginalLanguage();

    String getGenre();
    
}
