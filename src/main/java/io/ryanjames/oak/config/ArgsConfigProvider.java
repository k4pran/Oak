package io.ryanjames.oak.config;

import io.ryanjames.oak.CmdParser;
import io.ryanjames.oak.CommandLineException;
import io.ryanjames.oak.TextConfig;
import io.ryanjames.oak.VideoConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.Optional;

public class ArgsConfigProvider implements ConfigProvider {

    private String outputDir;
    private VideoConfig videoConfig;
    private TextConfig textConfig;

    public ArgsConfigProvider(String[] args) {
        parseArgs(args);
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

    private void parseArgs(String[] args) {
        Optional<CommandLine> parser;
        try {
            parser = CmdParser.parseCmdLine(args);
        } catch (ParseException e) {
            throw new ConfigLoadingException("Failed to parse arguments", e);
        }

        if (parser.isEmpty()) {
            throw new ConfigLoadingException("Unxpected exception when loading config from CLI");
        }

        try {
            videoConfig = CmdParser.loadVideoConfig(parser.get());
        } catch (CommandLineException e) {
            throw new RuntimeException("Failed to load video config", e);
        }

        try {
            textConfig = CmdParser.loadTextConfig(parser.get());
        } catch (CommandLineException e) {
            throw new RuntimeException("Failed to load text config" ,e);
        }
    }
}
