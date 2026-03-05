package io.ryanjames.oak;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ryanjames.oak.color.ColorConversionException;
import io.ryanjames.oak.color.ColorConversions;
import io.ryanjames.oak.config.ColorDeserializer;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextConfig {

    private static final Logger LOG = LoggerFactory.getLogger(TextConfig.class);

    private static final Color DEFAULT_COLOR = Color.BLACK;

    private CommandLine cmd;
    private String title;
    private CustomText titleText;
    private CustomText previewText;
    private CustomText generalText;
    private List<String> introText;
    private List<String> outroText;

    @JsonDeserialize(using = ColorDeserializer.class)
    private Color titleColor;

    public TextConfig() {
    }

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
            CustomText.setVideoIntroText(introList);
            CustomText.setPdfIntroText(new ArrayList<>(introList));
        }

        if(cmd.hasOption("outro")) {
            ArrayList<String> outroList = new ArrayList<>(Arrays.asList(cmd.getOptionValues("outro")));
            CustomText.setVideoOutroText(outroList);
            CustomText.setPdfOutroText(new ArrayList<>(outroList));
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Color getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(Color titleColor) {
        this.titleColor = titleColor;
    }
}
