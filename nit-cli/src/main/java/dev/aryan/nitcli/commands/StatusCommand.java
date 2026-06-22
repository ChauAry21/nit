package dev.aryan.nitcli.commands;

import dev.aryan.nitcli.client.NitApiClient;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "status", description = "Check backend and model connectivity")
public class StatusCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return new NitApiClient().checkStatus();
    }
}