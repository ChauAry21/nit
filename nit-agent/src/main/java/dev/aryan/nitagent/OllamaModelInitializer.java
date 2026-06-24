package dev.aryan.nitagent;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OllamaModelInitializer implements ApplicationRunner {

    private final RestClient restClient;
    private final String generatorModel;
    private final String criticModel;
    private final String ollamaBaseUrl;

    public OllamaModelInitializer(
            RestClient.Builder builder,
            @Value("${ollama.generator-model}") String generatorModel,
            @Value("${ollama.critic-model}") String criticModel,
            @Value("${ollama.base-url}") String ollamaBaseUrl
    ) {
        this.restClient = builder.baseUrl(ollamaBaseUrl).build();
        this.generatorModel = generatorModel;
        this.criticModel = criticModel;
        this.ollamaBaseUrl = ollamaBaseUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        pullAndWarm(generatorModel);
        pullAndWarm(criticModel);
    }

    private void pullAndWarm(String model) {
        try {
            restClient.post()
                    .uri("/api/pull")
                    .body(Map.of("model", model, "stream", false))
                    .retrieve()
                    .toBodilessEntity();

            restClient.post()
                    .uri("/api/generate")
                    .body(Map.of("model", model, "prompt", "", "stream", false))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            LoggerFactory.getLogger(OllamaModelInitializer.class)
                    .warn("Could not initialize model {}: {}", model, e.getMessage());
        }
    }
}
