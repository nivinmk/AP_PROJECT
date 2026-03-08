package com.example.home_gardening;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ApiService {
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:5000";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    private final String baseUrl;

    public ApiService() {
        String envUrl = System.getenv("ML_API_BASE_URL");
        this.baseUrl = (envUrl == null || envUrl.isBlank()) ? DEFAULT_BASE_URL : envUrl.trim();
    }

    public List<String> recommendPlants(String water, String space, String sunlight) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("water", normalizeLevel(water));
        payload.addProperty("space", normalizeLevel(space));
        payload.addProperty("sunlight", normalizeLevel(sunlight));

        JsonObject response = postJson("/recommend", payload);
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(response.get("recommendations"), listType);
    }

    public double predictSurvival(String plant, String water, String space, String sunlight) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("plant", plant);
        payload.addProperty("water", normalizeLevel(water));
        payload.addProperty("space", normalizeLevel(space));
        payload.addProperty("sunlight", normalizeLevel(sunlight));

        JsonObject response = postJson("/predict", payload);
        return response.get("survival_score").getAsDouble();
    }

    private JsonObject postJson(String endpoint, JsonObject payload) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (ConnectException | UnknownHostException | HttpTimeoutException e) {
            throw new IOException("Cannot reach ML backend at " + baseUrl + ". Start Flask server first.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request to ML backend was interrupted.", e);
        }

        JsonObject json;
        try {
            json = JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (Exception e) {
            throw new IOException("Backend returned invalid JSON. Body: " + response.body(), e);
        }

        if (response.statusCode() >= 400) {
            String message = json.has("error") ? json.get("error").getAsString() : "Backend error";
            throw new IOException(message + " (HTTP " + response.statusCode() + ")");
        }

        return json;
    }

    private String normalizeLevel(String value) throws IOException {
        if (value == null) {
            throw new IOException("Missing required input value.");
        }

        String normalized = value.trim().toLowerCase();
        Map<String, String> mapping = Map.of(
                "small", "low",
                "medium", "medium",
                "large", "high",
                "low", "low",
                "high", "high"
        );

        String mapped = mapping.get(normalized);
        if (mapped == null) {
            throw new IOException("Invalid level value: " + value);
        }
        return mapped;
    }
}
