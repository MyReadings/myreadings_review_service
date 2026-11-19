package org.modular.playground.review.infrastructure.client;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TokenCaptureResource implements QuarkusTestResourceLifecycleManager {
    private HttpServer server;
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                var authorization = exchange.getRequestHeaders().getFirst("Authorization");
                var cookie = exchange.getRequestHeaders().getFirst("Cookie");
                var field = exchange.getRequestURI().getPath().contains("/users") ? "username" : "title";
                var value = (authorization == null ? "missing" : authorization) + "|" + (cookie == null ? "no-cookie" : cookie);
                var body = ("[{\"" + field + "\":\"" + value + "\"}]").getBytes(StandardCharsets.UTF_8);
                exchange.getRequestBody().readAllBytes();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var out = exchange.getResponseBody()) { out.write(body); }
            });
            server.start();
            var url = "http://127.0.0.1:" + server.getAddress().getPort();
            return Map.of("quarkus.rest-client.catalog-service.url", url, "quarkus.rest-client.user-service.url", url);
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }
    public void stop() { if (server != null) server.stop(0); }
}
