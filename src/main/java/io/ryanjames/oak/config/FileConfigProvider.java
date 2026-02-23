package io.ryanjames.oak.config;

import io.ryanjames.oak.TextConfig;
import io.ryanjames.oak.VideoConfig;

public class FileConfigProvider implements ConfigProvider {

    private String outputDir;

    public FileConfigProvider(String configFilePath) {
        loadConfigFromFile(configFilePath);
    }

    @Override
    public String outputDir() {
        return outputDir;
    }

    @Override
    public VideoConfig videoConfig() {
        return null;
    }

    @Override
    public TextConfig textConfig() {
        return null;
    }

    private void loadConfigFromFile(String configFilePath) {
        // load config from file and set outputDir
    }
}
