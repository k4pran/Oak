package io.ryanjames.oak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.ryanjames.oak.TextConfig;
import io.ryanjames.oak.VideoConfig;

import java.io.InputStream;

public class FileConfigProvider implements ConfigProvider {

    private String outputDir;
    private VideoConfig videoConfig;
    private TextConfig textConfig;

    public FileConfigProvider(String resourceName) {
        loadConfigFromResources(resourceName);
    }

    @Override
    public String outputDir() {
        return outputDir;
    }

    @Override
    public VideoConfig videoConfig() {
        return videoConfig;
    }

    @Override
    public TextConfig textConfig() {
        return textConfig;
    }

    private void loadConfigFromResources(String resourceName) {

        InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName);

        if (is == null) {
            throw new ConfigLoadingException(
                    "Config file not found in resources: " + resourceName);
        }

        GlobalConfig config = null;
        try {
            config = load(resourceName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (config == null) {
            throw new ConfigLoadingException("Failed to parse YAML config");
        }

        this.outputDir = config.outputDir();
        this.videoConfig = config.videoConfig();
        this.textConfig = config.textConfig();
    }


    public static GlobalConfig load(String resourceName) throws Exception {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)) {

            if (is == null) {
                throw new IllegalArgumentException("Config not found: " + resourceName);
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(is, GlobalConfig.class);
        }
    }
}