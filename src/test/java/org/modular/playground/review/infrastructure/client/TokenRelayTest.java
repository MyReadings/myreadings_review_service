package org.modular.playground.review.infrastructure.client;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.modular.playground.review.web.controllers.ApiValidationTest;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(value = TokenCaptureResource.class, restrictToAnnotatedClass = true)
@TestProfile(TokenRelayTest.Profile.class)
@EnabledIfEnvironmentVariable(named = "DEMO_TEST_DB_URL", matches = ".+")
public class TokenRelayTest {
    public static class Profile extends ApiValidationTest.DatabaseProfile {
        @Override public Map<String, String> getConfigOverrides() {
            var values = new java.util.HashMap<>(super.getConfigOverrides());
            // Permit the test-only resource to inspect raw incoming headers; the
            // real internal endpoints' auth is verified by ApiValidationTest.
            values.put("quarkus.http.auth.proactive", "false");
            return values;
        }
    }
    @Test
    void relaysBearerTokensToBothClientsButNeverCookies() {
        given().header("Authorization", "Bearer demo-relay").header("Cookie", "private=do-not-forward")
                .get("/test/token-relay").then().statusCode(200)
                .body("catalog", is("Bearer demo-relay|no-cookie"))
                .body("users", is("Bearer demo-relay|no-cookie"));
    }
    @Test
    void doesNotInventServiceCredentialsWhenThereIsNoCaller() {
        given().get("/test/token-relay").then().statusCode(200)
                .body("catalog", is("missing|no-cookie"))
                .body("users", is("missing|no-cookie"));
    }
}
