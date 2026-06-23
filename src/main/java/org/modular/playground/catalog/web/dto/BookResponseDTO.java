package org.modular.playground.catalog.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDTO {

    private UUID bookId;
    private String isbn;
    private String title;
    private List<String> authors;
    private LocalDate publicationDate;
    private String publisher;
    private String description;
    private int pageCount;
    private String coverImageId;
    private String originalLanguage;
    private String genre;
}
