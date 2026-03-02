package io.ryanjames.oak.tools;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class ColorQuantizerCli {

    private ColorQuantizerCli() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        Path input = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        String paletteArg = args.length >= 3 ? args[2] : "#000000;#FFFFFF;#808080";

        List<Color> palette = ColorQuantizer.parsePalette(paletteArg);

        if (Files.isDirectory(input)) {
            try (Stream<Path> stream = Files.walk(input)) {
                stream.filter(Files::isRegularFile)
                        .filter(ColorQuantizer::isPng)
                        .forEach(path -> processFile(path, outputDir, palette));
            }
        } else {
            processFile(input, outputDir, palette);
        }
    }

    private static void processFile(Path input, Path outputDir, List<Color> palette) {
        try {
            Path output = ColorQuantizer.buildOutputPath(input, outputDir);
            ColorQuantizer.quantizeFile(input, output, palette);
            System.out.println("Wrote: " + output);
        } catch (IOException e) {
            System.err.println("Failed to process " + input + ": " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java io.ryanjames.oak.tools.ColorQuantizerCli <inputFileOrDir> <outputDir> [palette]");
        System.out.println("  palette format: hex (e.g. #000000;#FFFFFF;#808080) or rgb (e.g. 0,0,0;255,255,255)");
        System.out.println("  transparent pixels are preserved");
    }
}

