package dev.aryan.nitcli.commands;

import dev.aryan.nitcli.config.NitConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
        name = "config",
        description = "View or update CLI configuration",
        subcommands = {
                ConfigCommand.Get.class,
                ConfigCommand.Set.class
        }
)
public class ConfigCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        NitConfig.load().printAll();
        return 0;
    }

    @Command(name = "get", description = "Print a single config value")
    static class Get implements Callable<Integer> {
        @Parameters(index = "0") String key;

        @Override
        public Integer call() {
            System.out.println(NitConfig.load().get(key));
            return 0;
        }
    }

    @Command(name = "set", description = "Set a config value")
    static class Set implements Callable<Integer> {
        @Parameters(index = "0") String key;
        @Parameters(index = "1") String value;

        @Override
        public Integer call() {
            NitConfig config = NitConfig.load();
            config.set(key, value);
            config.save();
            return 0;
        }
    }
}