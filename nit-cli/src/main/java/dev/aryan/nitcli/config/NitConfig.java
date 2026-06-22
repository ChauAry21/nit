package dev.aryan.nitcli.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class NitConfig {

    private static final String CONFIG_DIR_PROPERTY = "nit.config.dir";

    private static Path getConfigDir() {
        String override = System.getProperty(CONFIG_DIR_PROPERTY);
        if (override != null) return Path.of(override);
        return Path.of(System.getProperty("user.home"), ".nit");
    }

    private static Path getConfigFile() {
        return getConfigDir().resolve("config.properties");
    }

    private final Properties props;

    private NitConfig(Properties props) {
        this.props = props;
    }

    public static NitConfig load() {
        Properties defaults = new Properties();
        try (InputStream in = NitConfig.class.getResourceAsStream("/default.properties")) {
            if (in != null) defaults.load(in);
        } catch (IOException ignored) {}

        Properties props = new Properties(defaults);
        Path configFile = getConfigFile();
        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Warning: could not read config file: " + e.getMessage());
            }
        }
        return new NitConfig(props);
    }

    public String getBackendUrl() {
        return props.getProperty("backend.url");
    }

    public String getGeneratorModel() {
        return props.getProperty("model.generator");
    }

    public String getCriticModel() {
        return props.getProperty("model.critic");
    }

    public void setGeneratorModel(String model) {
        props.setProperty("model.generator", model);
    }

    public void setCriticModel(String model) {
        props.setProperty("model.critic", model);
    }

    public String get(String key) {
        return props.getProperty(key, "");
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    public void save() {
        try {
            Path configDir = getConfigDir();
            Files.createDirectories(configDir);
            try (OutputStream out = Files.newOutputStream(configDir.resolve("config.properties"))) {
                props.store(out, "nit CLI configuration");
            }
        } catch (IOException e) {
            System.err.println("Error: could not save config file: " + e.getMessage());
        }
    }

    public void printAll() {
        props.forEach((k, v) -> System.out.println(k + "=" + v));
    }
}