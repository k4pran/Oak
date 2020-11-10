import color.ColorConversionException;
import color.ColorConversions;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TextConfig {

    private static final Logger LOG = LoggerFactory.getLogger(TextConfig.class);

    private static final Color DEFAULT_COLOR = Color.BLACK;

    private final CommandLine cmd;
    private String title;
    private Color titleColor;

    public TextConfig(CommandLine cmd) {
        this.cmd = cmd;
        this.title = "";
        this.titleColor = DEFAULT_COLOR;
        load();
    }

    private void load() {
        if(cmd.hasOption("t")) {
            title = cmd.getOptionValue("t");
        }

        if (cmd.hasOption("tc")) {
            try {
                titleColor = ColorConversions.interrogateColor(cmd.getOptionValue("tc"));
            }
            catch (ColorConversionException e) {
                LOG.warn("Invalid color: {} for title color. Defaulting to {}", cmd.getOptionValue("tc"), DEFAULT_COLOR);
                titleColor = Color.BLACK;
            }
        }
        CustomText.createTitleFont(title, titleColor);

        if(cmd.hasOption("intro")) {
            ArrayList<String> introList = new ArrayList<>(Arrays.asList(cmd.getOptionValues("intro")));
            CustomText.setIntroText(introList);
        }

        if(cmd.hasOption("outro")) {
            ArrayList<String> outroList = new ArrayList<>(Arrays.asList(cmd.getOptionValues("outro")));
            CustomText.setOutroText(outroList);
        }
    }
}
