package dev.aryan.nitcli.commands;

import dev.aryan.nitcli.client.NitApiClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "test", description = "Generate tests for a file")
public class TestCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "File to generate tests for")
    String path;

    @Override
    public Integer call() {
        return new NitApiClient().streamTestGeneration(path);
    }
}