package org.modular.playground.user.core.usecases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modular.playground.user.core.domain.User;
import org.eclipse.microprofile.jwt.JsonWebToken;

public interface UserService {
    Optional<User> findUserProfileById(UUID userId, JsonWebToken principal);
    Optional<User> findUserByIdInternal(UUID userId);
    List<User> findUsersByIds(List<UUID> userIds);
}
