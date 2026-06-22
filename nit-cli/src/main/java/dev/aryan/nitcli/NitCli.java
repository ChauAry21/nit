package dev.aryan.nitcli;

import dev.aryan.nitcli.commands.ConfigCommand;
import dev.aryan.nitcli.commands.ModelCommand;
import dev.aryan.nitcli.commands.ReviewCommand;
import dev.aryan.nitcli.commands.StatusCommand;
import dev.aryan.nitcli.commands.TestCommand;
import dev.aryan.nitcli.repl.ReplShell;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "nit",
        mixinStandardHelpOptions = true,
        version = "nit 0.1.0",
        subcommands = { ReviewCommand.class, TestCommand.class, ConfigCommand.class, StatusCommand.class, ModelCommand.class }
)
public class NitCli implements Runnable {

    @Option(names = {"--backend"}, description = "Override the backend base URL for this invocation")
    String backendUrl;

    public static void main(String[] args) {
        if (args.length == 0) {
            new ReplShell(new CommandLine(new NitCli())).start();
            return;
        }
        int exitCode = new CommandLine(new NitCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        new ReplShell(new CommandLine(this)).start();
    }
}