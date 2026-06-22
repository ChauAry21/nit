package dev.aryan.nitcli.commands;

import dev.aryan.nitcli.client.NitApiClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "review", mixinStandardHelpOptions = true, description = "Run a code review")
public class ReviewCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..1", description = "Local file or directory path to review")
    String path;

    @Option(names = "--github", description = "GitHub PR, commit, or file URL to review")
    String githubUrl;

    @Option(names = "--output", defaultValue = "text", description = "Output format: text or json")
    String outputFormat;

    @Override
    public Integer call() {
        NitApiClient client = new NitApiClient();
        if (githubUrl != null) {
            return client.streamGithubReview(githubUrl, outputFormat);
        }
        if (path != null) {
            return client.streamLocalReview(path, outputFormat);
        }
        System.err.println("Provide either a local path or --github <url>");
        return 1;
    }
}