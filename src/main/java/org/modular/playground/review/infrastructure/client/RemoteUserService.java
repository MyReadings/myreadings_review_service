package org.modular.playground.review.infrastructure.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.core.usecases.UserService;
import org.modular.playground.user.web.dto.UserResponseDTO;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RemoteUserService implements UserService {

    private static final Logger LOGGER = Logger.getLogger(RemoteUserService.class);

    @Inject
    @RestClient
    UserServiceClient userClient;

    @Override
    public Optional<User> findUserProfileById(UUID userId, JsonWebToken principal) {
        return findUserByIdInternal(userId);
    }

    @Override
    public Optional<User> findUserByIdInternal(UUID userId) {
        try {
            UserResponseDTO dto = userClient.getUserById(userId);
            return Optional.of(toUser(dto));
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public List<User> findUsersByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<UserResponseDTO> dtos = userClient.getUsersByIds(userIds);
            return dtos.stream().map(this::toUser).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.errorf(e, "Failed to fetch users from user-service");
            throw new ServiceUnavailableException("User service is unavailable", Response.status(503).build(), e);
        }
    }

    private User toUser(UserResponseDTO dto) {
        return UserImpl.builder()
                .keycloakUserId(dto.getUserId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .build();
    }
}
