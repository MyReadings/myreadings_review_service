package org.modular.playground.user.core.domain;

import java.util.UUID;


public interface User {

    UUID getKeycloakUserId();

    String getFirstName();

    String getLastName();

    String getUsername();

    String getEmail();

    UiTheme getThemePreference();

}
