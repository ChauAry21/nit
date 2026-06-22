package dev.aryan.nitcli.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aryan.nitcli.config.NitConfig;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class NitApiClient {

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String generatorModel;
    private final String criticModel;

    public NitApiClient() {
        NitConfig config = NitConfig.load();
        this.baseUrl = config.getBackendUrl();
        this.generatorModel = config.getGeneratorModel();
        this.criticModel = config.getCriticModel();
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public int streamGithubReview(String githubUrl, String outputFormat) {
        Map<String, String> body = new HashMap<>();
        body.put("url", githubUrl);
        body.put("generatorModel", generatorModel);
        body.put("criticModel", criticModel);
        return streamRequest("/review/github", body, outputFormat);
    }

    public int streamLocalReview(String path, String outputFormat) {
        Path target = Path.of(path);
        if (!Files.exists(target)) {
            System.err.println("Path not found: " + path);
            return 1;
        }
        Map<String, String> body = new HashMap<>();
        body.put("path", target.toAbsolutePath().toString());
        body.put("generatorModel", generatorModel);
        body.put("criticModel", criticModel);
        return streamRequest("/review", body, outputFormat);
    }

    public int streamTestGeneration(String path) {
        Path target = Path.of(path);
        if (!Files.exists(target)) {
            System.err.println("Path not found: " + path);
            return 1;
        }
        Map<String, String> body = new HashMap<>();
        body.put("path", target.toAbsolutePath().toString());
        body.put("generatorModel", generatorModel);
        body.put("criticModel", criticModel);
        return streamRequest("/test", body, "text");
    }

    public int listModels() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/models"))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            return 0;
        } catch (Exception e) {
            System.err.println("Could not reach backend at " + baseUrl);
            return 1;
        }
    }

    public int pullModel(String model) {
        try {
            String json = mapper.writeValueAsString(Map.of("model", model));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/models/pull"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofMinutes(30))
                    .build();
            System.out.println("Pulling " + model + ", this may take a while...");
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Pulled " + model + " successfully");
                return 0;
            }
            System.err.println("Pull failed with status " + response.statusCode());
            return 1;
        } catch (Exception e) {
            System.err.println("Error pulling model: " + e.getMessage());
            return 1;
        }
    }

    public int checkStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/actuator/health"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Backend: online (" + baseUrl + ")");
                System.out.println("Generator model: " + generatorModel);
                System.out.println("Critic model:    " + criticModel);
                return 0;
            }
            System.err.println("Backend returned status " + response.statusCode());
            return 1;
        } catch (Exception e) {
            System.err.println("Backend unreachable at " + baseUrl);
            System.err.println("Run 'nit config set backend.url <url>' to update the backend address.");
            return 1;
        }
    }

    private int streamRequest(String endpoint, Object body, String outputFormat) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofMinutes(10))
                    .build();

            HttpResponse<java.io.InputStream> response = http.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                System.err.println("Request failed with status " + response.statusCode());
                return 1;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (data.equals("[DONE]")) break;
                        if (outputFormat.equals("json")) {
                            System.out.println(data);
                        } else {
                            System.out.print(data);
                        }
                    }
                }
                System.out.println();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error communicating with backend: " + e.getMessage());
            return 1;
        }
    }
}