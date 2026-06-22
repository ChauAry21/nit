package dev.aryan.nitcli.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class NitApiClientTest {

    private WireMockServer server;
    private NitApiClient client;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        client = new NitApiClient(
                "http://localhost:" + server.port(),
                "qwen2.5-coder:14b",
                "qwen2.5-coder:7b"
        );
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void tearDown() {
        server.stop();
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void checkStatusReturnsZeroWhenBackendOnline() {
        server.stubFor(get(urlEqualTo("/actuator/health"))
                .willReturn(aResponse().withStatus(200).withBody("{\"status\":\"UP\"}")));

        int exitCode = client.checkStatus();

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("online"));
    }

    @Test
    void checkStatusReturnsOneWhenBackendDown() {
        server.stop();

        int exitCode = client.checkStatus();

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("unreachable"));
    }

    @Test
    void streamGithubReviewStreamsSseOutput() {
        String sseBody = "data: Reviewing your PR\ndata: Looks good\ndata: [DONE]\n";
        server.stubFor(post(urlEqualTo("/review/github"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/event-stream")
                        .withBody(sseBody)));

        int exitCode = client.streamGithubReview("https://github.com/owner/repo/pull/1", "text");

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("Reviewing your PR"));
        assertTrue(out.toString().contains("Looks good"));
    }

    @Test
    void streamGithubReviewSendsModelNames() {
        String sseBody = "data: done\ndata: [DONE]\n";
        server.stubFor(post(urlEqualTo("/review/github"))
                .withRequestBody(containing("qwen2.5-coder:14b"))
                .withRequestBody(containing("qwen2.5-coder:7b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/event-stream")
                        .withBody(sseBody)));

        int exitCode = client.streamGithubReview("https://github.com/owner/repo/pull/1", "text");

        assertEquals(0, exitCode);
        server.verify(postRequestedFor(urlEqualTo("/review/github"))
                .withRequestBody(containing("generatorModel")));
    }

    @Test
    void streamLocalReviewReturnsOneForMissingPath() {
        int exitCode = client.streamLocalReview("nonexistent/path/file.java", "text");

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Path not found"));
    }

    @Test
    void listModelsReturnsZeroOnSuccess() {
        server.stubFor(get(urlEqualTo("/models"))
                .willReturn(aResponse().withStatus(200).withBody("[\"qwen2.5-coder:14b\"]")));

        int exitCode = client.listModels();

        assertEquals(0, exitCode);
    }

    @Test
    void pullModelReturnsZeroOnSuccess() {
        server.stubFor(post(urlEqualTo("/models/pull"))
                .willReturn(aResponse().withStatus(200)));

        int exitCode = client.pullModel("llama3.1:8b");

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("successfully"));
    }

    @Test
    void pullModelReturnsOneOnFailure() {
        server.stubFor(post(urlEqualTo("/models/pull"))
                .willReturn(aResponse().withStatus(500)));

        int exitCode = client.pullModel("llama3.1:8b");

        assertEquals(1, exitCode);
    }
}