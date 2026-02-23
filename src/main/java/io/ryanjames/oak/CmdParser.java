package io.ryanjames.oak;

import io.ryanjames.oak.config.ConfigLoadingException;
import org.apache.commons.cli.*;

import java.util.Optional;

public class CmdParser {

    public static Optional<CommandLine> parseCmdLine(String[] args) throws ParseException {
        Options helpOptions = new Options();

        helpOptions.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display help.")
                .build());

        Options options = new Options();

        options.addOption(Option.builder("pdf")
                .desc("Outputs pdf only")
                .build());

        options.addOption(Option.builder("vid")
                .desc("Outputs video only")
                .build());

        options.addOption(Option.builder("i")
                .longOpt("file")
                .desc("Midi input file path. REQUIRED.")
                .hasArg()
                .argName("file")
                .required()
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .desc("Output file path.")
                .hasArgs()
                .argName("path")
                .required()
                .build()
        );

        options.addOption(Option.builder("b")
                .longOpt("background")
                .desc("Background image file path. If not set defaults to a white background.")
                .hasArg()
                .argName("file")
                .build()
        );

        options.addOption(Option.builder("a")
                .longOpt("audio")
                .desc("Audio file input. If not selected an audio file will be generated from the midi file.")
                .hasArg()
                .argName("file")
                .build()
        );

        options.addOption(Option.builder("d")
                .longOpt("dims")
                .desc("Number of rows/cols per page.")
                .hasArg()
                .argName("row/cols")
                .build()
        );

        options.addOption(Option.builder("pre")
                .desc("Add frames before tutorial. " +
                        "Enter image paths")
                .hasArgs()
                .argName("[path 1] [path 2]...")
                .numberOfArgs(10)
                .build()
        );

        options.addOption(Option.builder("post")
                .desc("Add frames after tutorial. " +
                        "Enter image paths")
                .hasArgs()
                .argName("[path 1] [path 2]...")
                .numberOfArgs(10)
                .build()
        );

        options.addOption(Option.builder("l")
                .longOpt("textPre")
                .desc("Displays frames of text along the bottom panel before the tutorial")
                .hasArg()
                .argName("text")
                .build()
        );

        options.addOption(Option.builder("j")
                .longOpt("textPost")
                .desc("Displays frames of text along the bottom panel after the tutorial")
                .hasArg()
                .argName("text")
                .build()
        );

        options.addOption(Option.builder("c")
                .longOpt("on")
                .desc("Note on colour for ocarina sprites. Default is 'yellow'.\n" +
                        "Accepted color formats:\n" +
                        "\tColor name - e.g. 'blue' or 'green' -- Accepts most basic colors.\n" +
                        "\tRGB - e.g. 255 0 0 -- R, G and B ranges 0-255, each divided by a space.\n" +
                        "\tHEX - e.g. FF 23 EE -- Hex value ranges 0-FF, each divided by a space.")
                .hasArg()
                .argName("io/ryanjames/oak/color")
                .build()
        );

        options.addOption(Option.builder("co")
                .longOpt("off")
                .desc("Note off colour for ocarina sprites. Default is 'red'.\n" +
                        "Accepted color formats:\n" +
                        "\tColor name - e.g. 'blue' or 'green' -- Accepts most basic colors.\n" +
                        "\tRGB - e.g. 255 0 0 -- R, G and B ranges 0-255, each divided by a space.\n" +
                        "\tHEX - e.g. FF 23 EE -- Hex value ranges 0-FF, each divided by a space.")
                .hasArg()
                .argName("io/ryanjames/oak/color")
                .build()
        );

        options.addOption(Option.builder("cp")
                .longOpt("preview")
                .desc("Preview colour for next ocarina sprite. Default is 'cyan'.\n" +
                        "Accepted color formats:\n" +
                        "\tColor name - e.g. 'blue' or 'green' -- Accepts most basic colors.\n" +
                        "\tRGB - e.g. 255 0 0 -- R, G and B ranges 0-255, each divided by a space.\n" +
                        "\tHEX - e.g. FF 23 EE -- Hex value ranges 0-FF, each divided by a space.")
                .hasArg()
                .argName("io/ryanjames/oak/color")
                .build()
        );

        options.addOption(Option.builder("fr")
                .longOpt("framerate")
                .desc("Video frame rate per second(fps). Default is '30fps'.")
                .hasArg()
                .argName("fps")
                .build()
        );

        options.addOption(Option.builder("ofs")
                .longOpt("offset")
                .desc("Offset in milliseconds to when the tutorial starts. \n" +
                        "Video will wait <ms> on first ocarina frame before beginning.")
                .hasArgs()
                .argName("ms")
                .build()
        );

        options.addOption(Option.builder("t")
                .longOpt("title")
                .desc("Music title text. REQUIRED.")
                .hasArg()
                .argName("text")
                .required()
                .build()
        );

        options.addOption(Option.builder("tc")
                .longOpt("title_color")
                .desc("Music title text color.")
                .hasArg()
                .argName("io/ryanjames/oak/color")
                .build()
        );

        options.addOption(Option.builder("intro")
                .desc("Text to appear at beginning of tutorial")
                .hasArgs()
                .argName("text")
                .build()
        );

        options.addOption(Option.builder("outro")
                .longOpt("title")
                .desc("Text to appear at end of tutorial")
                .hasArgs()
                .argName("text")
                .build()
        );

        if (new DefaultParser().parse(helpOptions, args, true).hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.setLeftPadding(10);
            helpFormatter.setWidth(helpFormatter.getWidth() + 40);
            helpFormatter.setLongOptPrefix(" --");
            helpFormatter.printHelp("Ocarina Factory", options);
            return Optional.empty();
        }

        CommandLineParser commandLineParser = new DefaultParser();
        try {
        return Optional.of(commandLineParser.parse(options, args));
        } catch (MissingOptionException e) {
            HelpFormatter formatter = new HelpFormatter();

            StringBuilder sb = new StringBuilder();
            sb.append("Failed to parse arguments.\n");
            sb.append("Provided args: ")
                    .append(String.join(" ", args))
                    .append("\n\n");

            sb.append("Missing required options: ")
                    .append(String.join(", ", e.getMissingOptions().stream()
                            .map(Object::toString)
                            .toList()))
                    .append("\n\n");

            sb.append("Example usage:\n");
            sb.append("  java -jar oak.jar -i input.mid -o output.wav -t Title\n\n");

            formatter.printHelp("oak", options);

            throw new ConfigLoadingException(sb.toString(), e);
        } catch (ParseException e) {
            throw new ConfigLoadingException("Argument parsing failed: "
                    + String.join(" ", args), e);
        }
    }

    public static VideoConfig loadVideoConfig(CommandLine cmd) throws CommandLineException {
        return new VideoConfig(cmd);
    }

    public static TextConfig loadTextConfig(CommandLine cmd) throws CommandLineException {
        return new TextConfig(cmd);
    }
}
