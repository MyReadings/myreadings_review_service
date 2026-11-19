package org.modular.playground.review.infrastructure.client;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;
import java.util.Map;

// Test-only REST entry point, never packaged with the application.
@Path("/test/token-relay")
@PermitAll
public class TokenRelayEndpoint {
    @Inject @RestClient CatalogServiceClient catalog;
    @Inject @RestClient UserServiceClient users;
    @GET
    public Map<String, String> relay() {
        return Map.of("catalog", catalog.getBooksByIds(List.of()).get(0).getTitle(),
                "users", users.getUsersByIds(List.of()).get(0).getUsername());
    }
}
