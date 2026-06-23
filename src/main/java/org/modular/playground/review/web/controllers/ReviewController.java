package org.modular.playground.review.web.controllers;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.core.domain.ReviewStatsImpl;
import org.modular.playground.review.core.usecases.ReviewService;
import org.modular.playground.review.infrastructure.persistence.postgres.mapper.ReviewMapper;
import org.modular.playground.review.web.dto.ReviewRequestDTO;
import org.modular.playground.review.web.dto.ReviewResponseDTO;
import org.modular.playground.review.web.dto.ReviewStatsResponseDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ReviewController {

    private static final Logger LOGGER = Logger.getLogger(ReviewController.class);

    @Inject
    ReviewService reviewService;

    @Inject
    JsonWebToken jwt;

    @Inject
    ReviewMapper reviewMapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response createReview(@Valid ReviewRequestDTO createReviewRequestDTO) {
        LOGGER.infof("Received request to create review for book ID: %s", createReviewRequestDTO.getBookId());
        Review createdReview = reviewService.createReview(createReviewRequestDTO, jwt);
        LOGGER.info("Review created successfully.");
        return Response.status(Response.Status.CREATED).entity(reviewMapper.toResponseDTO(createdReview)).build();
    }

    @GET
    @Path("/{reviewId}")
    @RolesAllowed({"user", "admin"})
    public Response getReviewById(@PathParam("reviewId") UUID reviewId) {
        LOGGER.infof("Received request to get review by ID: %s", reviewId);
        Review review = reviewService.findReviewAndCheckOwnership(reviewId, jwt);
        return Response.ok(reviewMapper.toResponseDTO(review)).build();
    }

    @PUT
    @Path("/{reviewId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Response updateReview(@PathParam("reviewId") UUID reviewId, @Valid ReviewRequestDTO updateReviewRequestDTO) {
        LOGGER.infof("Received request to update review with ID: %s", reviewId);
        Review updatedReview = reviewService.updateReview(reviewId, updateReviewRequestDTO, jwt);
        LOGGER.info("Review updated successfully.");
        return Response.ok(reviewMapper.toResponseDTO(updatedReview)).build();
    }

    @DELETE
    @Path("/{reviewId}")
    @RolesAllowed({"user", "admin"})
    public Response deleteReviewById(@PathParam("reviewId") UUID reviewId) {
        LOGGER.infof("Received request to delete review with ID: %s", reviewId);
        reviewService.deleteReviewById(reviewId, jwt);
        LOGGER.info("Review deleted successfully.");
        return Response.noContent().build();
    }

    @GET
    @Path("/books/{bookId}")
    @RolesAllowed({"user", "admin"})
    public Response getReviewsByBookId(@PathParam("bookId") UUID bookId) {
        LOGGER.infof("Received request to get reviews for book ID: %s", bookId);
        List<Review> reviews = reviewService.getReviewsForBook(bookId, jwt);
        List<ReviewResponseDTO> response = reviewMapper.toResponseDTOs(reviews);
        LOGGER.debugf("Found %d reviews for book ID: %s", response.size(), bookId);
        return Response.ok(response).build();
    }

    @GET
    @Path("/books/{bookId}/stats")
    @RolesAllowed({"user", "admin"})
    public Response getBookReviewStats(@PathParam("bookId") UUID bookId) {
        LOGGER.infof("Received request for review stats for book ID: %s", bookId);
        ReviewStatsImpl stats = reviewService.getReviewStatsForBook(bookId);
        ReviewStatsResponseDTO responseDTO = reviewMapper.toStatsResponseDTO(stats, bookId);
        return Response.ok(responseDTO).build();
    }

    @GET
    @Path("/books/{bookId}/my-review")
    @RolesAllowed({"user", "admin"})
    public Response getMyReviewForBook(@PathParam("bookId") UUID bookId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        LOGGER.infof("Received request for user %s's review for book ID: %s", currentUserId, bookId);
        Review review = reviewService.findReviewByUserAndBook(currentUserId, bookId, jwt)
                .orElseThrow(() -> new NotFoundException("Review not found for this user and book."));
        return Response.ok(reviewMapper.toResponseDTO(review)).build();
    }

    @GET
    @Path("/users/{userId}")
    @RolesAllowed({"user", "admin"})
    public Response getReviewsByUserId(@PathParam("userId") UUID userId) {
        LOGGER.infof("Received request to get reviews for user ID: %s", userId);
        List<Review> reviews = reviewService.getReviewsForUser(userId, jwt);
        List<ReviewResponseDTO> response = reviewMapper.toResponseDTOs(reviews);
        LOGGER.debugf("Found %d reviews for user ID: %s", response.size(), userId);
        return Response.ok(response).build();
    }
}
