package org.modular.playground.catalog.core.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.Builder.Default;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@io.quarkus.runtime.annotations.RegisterForReflection
public class BookImpl implements Book {

    @EqualsAndHashCode.Include
    private UUID bookId;

    @NotBlank
    private String isbn;

    @NotBlank
    @Size(max = 255)
    private String title;

    @Default
    private List<String> authors = new ArrayList<>();

    @PastOrPresent
    private LocalDate publicationDate;

    @Size(max = 255)
    private String publisher;

    @Size(max = 500)
    private String description;

    @Min(0)
    private int pageCount;

    @Size(max = 255)
    private String coverImageId;

    @Size(max = 50)
    private String originalLanguage;

    @Size(max = 50)
    private String genre;
}
