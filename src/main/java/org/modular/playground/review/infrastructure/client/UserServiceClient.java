package org.modular.playground.review.infrastructure.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.modular.playground.user.web.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "user-service")
@Path("/api/internal/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserServiceClient {

    @GET
    @Path("/{userId}")
    UserResponseDTO getUserById(@PathParam("userId") UUID userId);

    @POST
    @Path("/batch")
    List<UserResponseDTO> getUsersByIds(List<UUID> userIds);
}
