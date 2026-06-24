package org.modular.playground.review.infrastructure.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.modular.playground.user.web.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "user-service")
@Path("/api/internal/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
@Timeout(3000)
public interface UserServiceClient {

    @GET
    @Path("/{userId}")
    UserResponseDTO getUserById(@PathParam("userId") UUID userId);

    @POST
    @Path("/batch")
    List<UserResponseDTO> getUsersByIds(List<UUID> userIds);
}
