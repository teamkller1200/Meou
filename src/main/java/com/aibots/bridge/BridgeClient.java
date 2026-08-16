package com.aibots.bridge;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.aibots.Aibots;

/**
 * Client that sends context payloads to the Python bridge server on a background
 * thread so the server tick thread is never blocked by network I/O.
 */
public class BridgeClient {
    private final String endpoint;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final Supplier<String> payloadSupplier;
    private final Consumer<String> responseConsumer;

    public BridgeClient(String endpoint,
                        Supplier<String> payloadSupplier,
                        Consumer<String> responseConsumer) {
        this.endpoint = endpoint;
        this.payloadSupplier = payloadSupplier;
        this.responseConsumer = responseConsumer;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start(long periodSeconds) {
        scheduler.scheduleWithFixedDelay(this::sendCurrentPayload, 0, periodSeconds, TimeUnit.SECONDS);
        Aibots.LOGGER.info("BridgeClient connected to {}", endpoint);
    }

    public void stop() {
        scheduler.shutdownNow();
        Aibots.LOGGER.info("BridgeClient stopped");
    }

    /** Send the payload produced by {@link #payloadSupplier} to the bridge. */
    public void sendCurrentPayload() {
        try {
            String payload = payloadSupplier.get();
            if (payload == null) {
                return;
            }
            send(payload);
        } catch (Exception ex) {
            Aibots.LOGGER.warn("Bridge sendCurrentPayload error: {}", ex.getMessage());
        }
    }

    /** Send a specific payload supplied by {@code supplier} to the bridge immediately. */
    public void sendCurrentPayload(Supplier<String> supplier) {
        try {
            String payload = supplier.get();
            if (payload == null) {
                return;
            }
            send(payload);
        } catch (Exception ex) {
            Aibots.LOGGER.warn("Bridge sendCurrentPayload error: {}", ex.getMessage());
        }
    }

    private void send(String payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseConsumer)
                .exceptionally(ex -> {
                    Aibots.LOGGER.warn("Bridge request failed: {}", ex.getMessage());
                    return null;
                });
        } catch (Exception ex) {
            Aibots.LOGGER.warn("Bridge send error: {}", ex.getMessage());
        }
    }
}