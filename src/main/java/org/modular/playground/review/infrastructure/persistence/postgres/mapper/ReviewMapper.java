package org.modular.playground.review.infrastructure.persistence.postgres.mapper;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewImpl;
import org.modular.playground.review.core.domain.ReviewStatsImpl;
import org.modular.playground.review.infrastructure.persistence.postgres.ReviewEntity;
import org.modular.playground.review.web.dto.ReviewRequestDTO;
import org.modular.playground.review.web.dto.ReviewResponseDTO;
import org.modular.playground.review.web.dto.ReviewStatsResponseDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = { UUID.class, LocalDateTime.class })
public interface ReviewMapper {

    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "publicationDate", expression = "java(LocalDateTime.now())")
    ReviewImpl toDomain(ReviewRequestDTO dto, User user, Book book);

    @Mapping(source = "book.bookId", target = "bookId")
    @Mapping(source = "user.keycloakUserId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    ReviewResponseDTO toResponseDTO(Review review);

    List<ReviewResponseDTO> toResponseDTOs(List<Review> reviews);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "reviewId", ignore = true) 
    @Mapping(source = "book.bookId", target = "bookId")
    @Mapping(source = "user.keycloakUserId", target = "userId")
    ReviewEntity toEntity(Review domain);

    @Mapping(source = "bookId", target = "book", qualifiedByName = "mapBookIdToBookStub")
    @Mapping(source = "userId", target = "user", qualifiedByName = "mapUserIdToUserStub")
    ReviewImpl toDomain(ReviewEntity entity);

    @Mapping(source = "bookId", target = "bookId")
    ReviewStatsResponseDTO toStatsResponseDTO(ReviewStatsImpl stats, UUID bookId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "publicationDate", ignore = true)
    void updateFromDto(ReviewRequestDTO dto, @MappingTarget ReviewImpl review);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "bookId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "publicationDate", ignore = true)
    void updateEntityFromDomain(Review domain, @MappingTarget ReviewEntity entity);

    @Named("mapBookIdToBookStub")
    default Book mapBookIdToBookStub(UUID bookId) {
        if (bookId == null)
            return null;
        return BookImpl.builder().bookId(bookId).build();
    }

    @Named("mapUserIdToUserStub")
    default User mapUserIdToUserStub(UUID userId) {
        if (userId == null)
            return null;
        return UserImpl.builder().keycloakUserId(userId).build();
    }
}
