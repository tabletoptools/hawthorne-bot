package io.tabletoptools.hawthorne;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import org.json.JSONObject;
import sun.rmi.rmic.iiop.ClassPathLoader;

import java.io.IOException;
import java.net.URL;

public class Config {

    private static Config instance;
    private JSONObject configJson;

    private Config(JSONObject configJson) {
        this.configJson = configJson;
    }

    public static Config instance() {
        if (instance == null) {
            try {
                instance = loadConfig();
            } catch (IOException ex) {
                Loggers.APPLICATION_LOG.error("Could not read configuration file.");
            }
        }
        return instance;
    }

    private static Config loadConfig() throws IOException {

        if (!System.getenv().containsKey("env")) {
            Loggers.APPLICATION_LOG.error("Environment variable <env> not present.");
            throw new IOException();
        }
        String environment = System.getenv("env");
        URL configURL = ClassPathLoader.getSystemClassLoader().getResource("environments.json");
        if (configURL == null) {
            Loggers.APPLICATION_LOG.error("Configuration file not present: <environments.json>.");
            throw new IOException();
        }
        String source = Resources.toString(configURL, Charsets.UTF_8);
        JSONObject configJson = new JSONObject(source);
        configJson = configJson.getJSONObject(environment);
        return new Config(configJson);
    }

    public JSONObject getConfigurationEntries() {
        return this.configJson.getJSONObject("configurationEntries");
    }

    public Object getEntry(String key) {
        return this.getConfigurationEntries().get(key);
    }

    public Boolean getBoolean(String key) {
        return this.getConfigurationEntries().getBoolean(key);
    }

    public String getString(String key) {
        return this.getConfigurationEntries().getString(key);
    }

    public Integer getInteger(String key) {
        return this.getConfigurationEntries().getInt(key);
    }

    public <T> T getEntity(String key, Class<T> tClass) {
        return tClass.cast(this.getEntry(key));
    }

}
