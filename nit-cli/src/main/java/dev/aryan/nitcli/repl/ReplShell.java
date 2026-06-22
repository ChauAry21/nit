package dev.aryan.nitcli.repl;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;

import java.io.IOException;

public class ReplShell {

    private final CommandLine cli;

    public ReplShell(CommandLine cli) {
        this.cli = cli;
    }

    public void start() {
        try {
            Terminal terminal = TerminalBuilder.builder().build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            System.out.println("nit interactive shell. Type 'help' for commands, 'exit' to quit.");
            while (true) {
                String line = reader.readLine("nit> ").trim();
                if (line.isEmpty()) continue;
                if (line.equals("exit") || line.equals("quit")) break;
                cli.execute(line.split("\\s+"));
            }
        } catch (IOException e) {
            System.err.println("Failed to start shell: " + e.getMessage());
        }
    }
}