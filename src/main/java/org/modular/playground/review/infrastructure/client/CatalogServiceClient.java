package org.modular.playground.review.infrastructure.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.modular.playground.catalog.web.dto.BookResponseDTO;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "catalog-service")
@Path("/api/internal/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CatalogServiceClient {

    @POST
    @Path("/batch")
    List<BookResponseDTO> getBooksByIds(List<UUID> bookIds);

    @GET
    @Path("/{bookId}")
    BookResponseDTO getBookById(@PathParam("bookId") UUID bookId);
}
