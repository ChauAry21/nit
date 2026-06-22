package dev.aryan.nitcli.commands;

import dev.aryan.nitcli.config.NitConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ModelCommandTest {

    @TempDir
    Path tempDir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setProperty("nit.config.dir", tempDir.toString());
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("nit.config.dir");
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void showPrintsDefaultModels() {
        int exitCode = new CommandLine(new ModelCommand()).execute("show");

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("qwen2.5-coder:14b"));
        assertTrue(out.toString().contains("qwen2.5-coder:7b"));
    }

    @Test
    void setGeneratorUpdatesConfig() {
        new CommandLine(new ModelCommand()).execute("set", "generator", "llama3.1:8b");

        NitConfig config = NitConfig.load();
        assertEquals("llama3.1:8b", config.getGeneratorModel());
    }

    @Test
    void setCriticUpdatesConfig() {
        new CommandLine(new ModelCommand()).execute("set", "critic", "llama3.2:3b");

        NitConfig config = NitConfig.load();
        assertEquals("llama3.2:3b", config.getCriticModel());
    }

    @Test
    void setInvalidRoleReturnsExitCode1() {
        int exitCode = new CommandLine(new ModelCommand()).execute("set", "unknown", "llama3.1:8b");

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Role must be 'generator' or 'critic'"));
    }

    @Test
    void showReflectsUpdatedModels() {
        new CommandLine(new ModelCommand()).execute("set", "generator", "mistral:7b");

        out.reset();
        new CommandLine(new ModelCommand()).execute("show");

        assertTrue(out.toString().contains("mistral:7b"));
    }
}