package io.ryanjames.oak.config;

import io.ryanjames.oak.TextConfig;
import io.ryanjames.oak.VideoConfig;

public record GlobalConfig(String outputDir, VideoConfig videoConfig, TextConfig textConfig) {
}
