import org.apache.commons.cli.*;

public class CmdParser {

    //================================================================================
    // Properties
    //================================================================================

    CommandLine cmd;

    //================================================================================
    // Constructors
    //================================================================================

    public CmdParser(String[] args) throws CommandLineException {
        cmd = parseCmdLine(args);
    }

    //================================================================================
    // General methods
    //================================================================================

    public void loadFromCmdLine() throws CommandLineException {
        new SettingsLoader(cmd);
        new VideoPartsLoader(cmd);
        new TextLoader(cmd);
    }

    public CommandLine parseCmdLine(String[] args) throws CommandLineException {
        Options options = new Options();

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display help.")
                .build());

        options.addOption(Option.builder("gui")
                .desc("launch gui setup. This will ignore any further commandline arguments")
                .build());

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
                .argName("<file>")
                .required()
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .desc("Output file path.")
                .hasArgs()
                .argName("<path>")
                .build()
        );

        options.addOption(Option.builder("b")
                .longOpt("background")
                .desc("Background image file path. If not set defaults to a white background.")
                .hasArg()
                .argName("<file>")
                .build()
        );

        options.addOption(Option.builder("a")
                .longOpt("audio")
                .desc("Audio file input. If not selected an audio file will be generated from the midi file.")
                .hasArg()
                .argName("<file>")
                .build()
        );

        options.addOption(Option.builder("pre")
                .desc("Add frames before tutorial. " +
                        "Enter image paths")
                .hasArgs()
                .argName("<path 1> <path 2>...")
                .numberOfArgs(10)
                .build()
        );

        options.addOption(Option.builder("post")
                .desc("Add frames after tutorial. " +
                        "Enter image paths")
                .hasArgs()
                .argName("<path 1> <path 2>...")
                .numberOfArgs(10)
                .build()
        );

        options.addOption(Option.builder("l")
                .longOpt("textPre")
                .desc("Displays frames of text along the bottom panel before the tutorial")
                .hasArg()
                .argName("<text>")
                .build()
        );

        options.addOption(Option.builder("j")
                .longOpt("textPost")
                .desc("Displays frames of text along the bottom panel after the tutorial")
                .hasArg()
                .argName("<text>")
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
                .argName("<color>")
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
                .argName("<color>")
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
                .argName("<color>")
                .build()
        );

        options.addOption(Option.builder("fr")
                .longOpt("framerate")
                .desc("Video frame rate per second(fps). Default is '30fps'.")
                .hasArg()
                .argName("<fps>")
                .build()
        );

        options.addOption(Option.builder("ofs")
                .longOpt("offset")
                .desc("Offset in milliseconds to when the tutorial starts. \n" +
                        "Video will wait <ms> on first ocarina frame before beginning.")
                .hasArgs()
                .argName("<ms>")
                .build()
        );

        options.addOption(Option.builder("t")
                .longOpt("title")
                .desc("Music title text. REQUIRED.")
                .hasArg()
                .argName("<text>")
                .required()
                .build()
        );

        options.addOption(Option.builder("intro")
                .desc("Text to appear at beginning of tutorial")
                .hasArgs()
                .argName("<text>")
                .build()
        );

        options.addOption(Option.builder("outro")
                .longOpt("title")
                .desc("Text to appear at end of tutorial")
                .hasArgs()
                .argName("<text>")
                .build()
        );


        try {
            org.apache.commons.cli.CommandLineParser commandLineParser = new DefaultParser();
            CommandLine cmd = commandLineParser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setLeftPadding(10);
                helpFormatter.setWidth(helpFormatter.getWidth() + 40);
                helpFormatter.setLongOptPrefix(" --");
                helpFormatter.printHelp("Ocarina Factory", options);
                return null;
            }


            return cmd;
        }
        catch (ParseException e) {
            e.printStackTrace();
            throw new CommandLineException("Unable to parse command line.");
        }
    }
}
