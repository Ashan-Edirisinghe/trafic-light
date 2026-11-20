package org.example.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.TrafficStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TrafficApiClient {
    private static final String BASE_URL = "http://localhost:8080/api/traffic";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TrafficApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public TrafficStatus getStatus() throws ApiException, BackendUnavailableException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/status"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        return send(request);
    }

    public TrafficStatus start() throws ApiException, BackendUnavailableException {
        return sendPost("/start");
    }

    public TrafficStatus pause() throws ApiException, BackendUnavailableException {
        return sendPost("/pause");
    }

    public TrafficStatus resume() throws ApiException, BackendUnavailableException {
        return sendPost("/resume");
    }

    public TrafficStatus stop() throws ApiException, BackendUnavailableException {
        return sendPost("/stop");
    }

    public TrafficStatus reset() throws ApiException, BackendUnavailableException {
        return sendPost("/reset");
    }

    public TrafficStatus emergency() throws ApiException, BackendUnavailableException {
        return sendPost("/emergency");
    }

    public TrafficStatus setManualLight(String light) throws ApiException, BackendUnavailableException {
        return sendPost("/manual/" + light.trim().toUpperCase());
    }

    private TrafficStatus sendPost(String path) throws ApiException, BackendUnavailableException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(2))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request);
    }

    private TrafficStatus send(HttpRequest request) throws ApiException, BackendUnavailableException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status >= 200 && status < 300) {
                return objectMapper.readValue(body, TrafficStatus.class);
            }

            String errorMsg = extractErrorMessage(body, status);
            throw new ApiException(status, errorMsg);
        } catch (IOException e) {
            throw new BackendUnavailableException("Cannot connect to backend server at " + BASE_URL, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackendUnavailableException("Request interrupted", e);
        }
    }

    private String extractErrorMessage(String body, int status) {
        if (body == null || body.isBlank()) {
            return "Request rejected (HTTP " + status + ")";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("message") && !root.get("message").asText().isBlank()) {
                return root.get("message").asText();
            }
            if (root.has("error") && !root.get("error").asText().isBlank()) {
                return root.get("error").asText();
            }
        } catch (IOException ignored) {
            // Fall back to plain text response
        }
        return body;
    }
}
