import color.ColorConversions;
import org.apache.commons.cli.CommandLine;

import java.awt.*;

public class SettingsLoader {

    //================================================================================
    // Properties
    //================================================================================

    private int framerate;
    private Double audioOffset;
    private Color noteOnColor;
    private Color noteOffColor;
    private Color previewNoteColor;

    private CommandLine cmd;

    //================================================================================
    // Constructors
    //================================================================================

    public SettingsLoader(CommandLine cmd) throws CommandLineException {
        this.cmd = cmd;
        Conductor.setSettings(load());
    }

    //================================================================================
    // General methods
    //================================================================================

    private Settings load() throws CommandLineException {

        if(cmd.hasOption("c")) {
            Color color = ColorConversions.interrogateColor(cmd.getOptionValue("c"));
            if(color != null) {
                noteOnColor = color;
            }
            else {
                throw new CommandLineException("Invalid color format (Note on color)");
            }
        }

        if(cmd.hasOption("co")) {
            Color color = ColorConversions.interrogateColor(cmd.getOptionValue("co"));
            if(color != null) {
                noteOffColor = color;
            }
            else {
                throw new CommandLineException("Invalid color format (Note off color)");
            }
        }

        if(cmd.hasOption("cp")) {
            Color color = ColorConversions.interrogateColor(cmd.getOptionValue("cp"));
            if(color != null) {
                previewNoteColor = color;
            }
            else {
                throw new CommandLineException("Invalid color format (Note preview color)");
            }
        }

        if(cmd.hasOption("fr")) {
            try {
                int fps = Integer.parseInt(cmd.getOptionValue("fr"));
                if(fps > 0 && fps <= 1200) {
                    framerate = fps;
                }
                else {
                    System.out.println("Invalid fps. \n\tMIN - 1\n\tMAX - 1200");
                    System.out.println("Defaulting to 60fps");
                }
            }
            catch(NumberFormatException e) {
                System.out.println("Invalid fps. Must be numerical value of number of frames per second.");
                System.out.println("Defaulting to 60fps");
                framerate = 60;
            }
        }
        else {
            framerate = 60;
        }

        if(cmd.hasOption("ofs")) {
            try {
                Double offset = Double.parseDouble(cmd.getOptionValue("ofs"));
                audioOffset = offset;
            }
            catch(NumberFormatException e) {
                System.out.println("Invalid offset. Must be a numerical offset value in seconds.");
                throw new CommandLineException("Invalid audio offset");
            }
        }
        else {
            audioOffset = 0.2;
        }

        return new Settings(
                    framerate, audioOffset,
                noteOnColor, noteOffColor, previewNoteColor
        );
    }
}
