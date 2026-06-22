package dev.aryan.nitcli.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class NitConfig {

    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.home"), ".nit");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");

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
        if (Files.exists(CONFIG_FILE)) {
            try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
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
            Files.createDirectories(CONFIG_DIR);
            try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
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