package io.ryanjames.oak.config;

import io.ryanjames.oak.TextConfig;
import io.ryanjames.oak.VideoConfig;

public class RequestConfigProvider implements ConfigProvider {

    @Override
    public String outputDir() {
        return "";
    }

    @Override
    public VideoConfig videoConfig() {
        return null;
    }

    @Override
    public TextConfig textConfig() {
        return null;
    }
}
