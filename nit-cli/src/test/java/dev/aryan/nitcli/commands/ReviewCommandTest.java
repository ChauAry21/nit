package dev.aryan.nitcli.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class ReviewCommandTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void noArgsReturnsExitCode1() {
        int exitCode = new CommandLine(new ReviewCommand()).execute();

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Provide either a local path or --github"));
    }

    @Test
    void missingPathReturnsExitCode1() {
        int exitCode = new CommandLine(new ReviewCommand()).execute("nonexistent/path/file.java");

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("Path not found"));
    }

    @Test
    void helpFlagPrintsUsage() {
        StringWriter helpOut = new StringWriter();
        CommandLine cmd = new CommandLine(new ReviewCommand());
        cmd.setOut(new PrintWriter(helpOut));

        cmd.execute("--help");

        assertTrue(helpOut.toString().contains("review"));
    }
}