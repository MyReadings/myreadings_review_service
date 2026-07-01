package org.modular.playground.user.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modular.playground.user.core.domain.UiTheme;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@io.quarkus.runtime.annotations.RegisterForReflection
public class UserResponseDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    @Builder.Default
    private UiTheme themePreference = UiTheme.LIGHT;
}
