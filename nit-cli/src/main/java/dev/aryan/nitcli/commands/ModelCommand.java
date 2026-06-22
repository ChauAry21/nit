package dev.aryan.nitcli.commands;

import dev.aryan.nitcli.client.NitApiClient;
import dev.aryan.nitcli.config.NitConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
        name = "model",
        description = "Manage Ollama models used by nit",
        subcommands = {
                ModelCommand.List.class,
                ModelCommand.Pull.class,
                ModelCommand.Set.class,
                ModelCommand.Show.class
        }
)
public class ModelCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        NitConfig config = NitConfig.load();
        System.out.println("generator: " + config.getGeneratorModel());
        System.out.println("critic:    " + config.getCriticModel());
        return 0;
    }

    @Command(name = "list", description = "List models available in Ollama")
    static class List implements Callable<Integer> {
        @Override
        public Integer call() {
            return new NitApiClient().listModels();
        }
    }

    @Command(name = "pull", description = "Pull a model via Ollama")
    static class Pull implements Callable<Integer> {
        @Parameters(index = "0", description = "Model name to pull, e.g. qwen2.5-coder:14b") String model;

        @Override
        public Integer call() {
            return new NitApiClient().pullModel(model);
        }
    }

    @Command(name = "set", description = "Set the generator or critic model")
    static class Set implements Callable<Integer> {
        @Parameters(index = "0", description = "Role: generator or critic") String role;
        @Parameters(index = "1", description = "Model name, e.g. llama3.1:8b") String model;

        @Override
        public Integer call() {
            if (!role.equals("generator") && !role.equals("critic")) {
                System.err.println("Role must be 'generator' or 'critic'");
                return 1;
            }
            NitConfig config = NitConfig.load();
            if (role.equals("generator")) {
                config.setGeneratorModel(model);
            } else {
                config.setCriticModel(model);
            }
            config.save();
            System.out.println(role + " model set to " + model);
            return 0;
        }
    }

    @Command(name = "show", description = "Show currently configured models")
    static class Show implements Callable<Integer> {
        @Override
        public Integer call() {
            NitConfig config = NitConfig.load();
            System.out.println("generator: " + config.getGeneratorModel());
            System.out.println("critic:    " + config.getCriticModel());
            return 0;
        }
    }
}