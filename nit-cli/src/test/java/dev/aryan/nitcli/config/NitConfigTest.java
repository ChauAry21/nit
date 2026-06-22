package dev.aryan.nitcli.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NitConfigTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty("nit.config.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("nit.config.dir");
    }

    @Test
    void defaultsLoadFromBundledProperties() {
        NitConfig config = NitConfig.load();
        assertEquals("http://localhost:8080", config.getBackendUrl());
        assertEquals("qwen2.5-coder:14b", config.getGeneratorModel());
        assertEquals("qwen2.5-coder:7b", config.getCriticModel());
    }

    @Test
    void savedValueOverridesDefault() {
        NitConfig config = NitConfig.load();
        config.set("backend.url", "http://localhost:9090");
        config.save();

        NitConfig reloaded = NitConfig.load();
        assertEquals("http://localhost:9090", reloaded.getBackendUrl());
    }

    @Test
    void setGeneratorModelPersists() {
        NitConfig config = NitConfig.load();
        config.setGeneratorModel("llama3.1:8b");
        config.save();

        NitConfig reloaded = NitConfig.load();
        assertEquals("llama3.1:8b", reloaded.getGeneratorModel());
    }

    @Test
    void setCriticModelPersists() {
        NitConfig config = NitConfig.load();
        config.setCriticModel("llama3.2:3b");
        config.save();

        NitConfig reloaded = NitConfig.load();
        assertEquals("llama3.2:3b", reloaded.getCriticModel());
    }

    @Test
    void getMissingKeyReturnsEmptyString() {
        NitConfig config = NitConfig.load();
        assertEquals("", config.get("nonexistent.key"));
    }

    @Test
    void userConfigDoesNotAffectDefaultsForOtherKeys() {
        NitConfig config = NitConfig.load();
        config.set("backend.url", "http://custom:8080");
        config.save();

        NitConfig reloaded = NitConfig.load();
        assertEquals("qwen2.5-coder:14b", reloaded.getGeneratorModel());
        assertEquals("qwen2.5-coder:7b", reloaded.getCriticModel());
    }
}