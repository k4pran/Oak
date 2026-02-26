package io.ryanjames.oak;

import io.ryanjames.oak.color.ColorConversions;
import io.ryanjames.oak.imagemod.ImageProcessingException;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.ryanjames.oak.validation.Validator;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class VideoConfig {

    private static final Logger LOG = LoggerFactory.getLogger(VideoConfig.class);

    private static final int MIN_FPS = 60;
    private static final int MAX_FPS = 144;

    private boolean outputPdf;
    private boolean outputVid = true;
    private int framerate = 60;
    private double audioOffset;
    private Color noteOnColor = Color.RED;
    private Color noteOffColor = Color.BLUE;
    private Color previewNoteColor = Color.GREEN;
    private int dims = 3;
    private String midiFilePath;
    private File background; // Todo likely also remove
    private String audioFilePath;
    private String outputFilePath;
    private boolean audioFromMidi = true;

    public static VideoConfig fromCmd(CommandLine cmd) throws CommandLineException {
        VideoConfig config = new VideoConfig();
        config.load(cmd);
        return config;
    }

    private void load(CommandLine cmd) throws CommandLineException {

        outputPdf = cmd.hasOption("pdf");
        outputVid = cmd.hasOption("vid");
        if (!outputPdf && !outputVid) {
            LOG.info("pdf and vid options not specified. Defaulting to both enabled.");
            outputPdf = outputVid = true;
        }

        if(cmd.hasOption("d")) {
            try {
                dims = Integer.parseInt(cmd.getOptionValue("d"));
            }
            catch (NumberFormatException e) {
                LOG.warn("Invalid dimensions, defaulting to 3 x 3");
            }
        }

        if(cmd.hasOption("c")) {
            noteOnColor = ColorConversions.interrogateColor(cmd.getOptionValue("c"));
        }
        if(cmd.hasOption("co")) {
            noteOffColor = ColorConversions.interrogateColor(cmd.getOptionValue("co"));
        }
        if(cmd.hasOption("cp")) {
            previewNoteColor = ColorConversions.interrogateColor(cmd.getOptionValue("cp"));
        }

        if(cmd.hasOption("fr")) {
            try {
                int fps = Integer.parseInt(cmd.getOptionValue("fr"));
                if(fps > MIN_FPS && fps <= MAX_FPS) {
                    framerate = fps;
                }
                else {
                    LOG.warn("Invalid fps. Allowed values range: {}-{}. Defaulting to {}", MIN_FPS, MAX_FPS, framerate);
                }
            }
            catch(NumberFormatException e) {
                LOG.warn("Invalid fps. Must be numerical value of number of frames per second, defaulting to {}", framerate);
            }
        }

        if(cmd.hasOption("ofs")) {
            try {
                audioOffset = Double.parseDouble(cmd.getOptionValue("ofs"));
            }
            catch(NumberFormatException e) {
                LOG.warn("Invalid offset. Must be a numerical offset value in seconds. Defaulting to no offset");
            }
        }

        if(cmd.hasOption("i")) {
            this.midiFilePath = cmd.getOptionValue("i");
//            loadMidiInput(midiPath);
        }

        if(cmd.hasOption("o")) {
            String path = cmd.getOptionValue("o");
            if(Validator.isValidPath(path)) {
                outputFilePath = path;
            }
            else {
                throw new CommandLineException("Output file path is not valid: " + cmd.getOptionValue("o"));
            }
        }

        if(cmd.hasOption("b")) {
            if(Validator.isValidImageFile(cmd.getOptionValue("b"))) {
                background = new File(cmd.getOptionValue("b"));
            }
            else {
                throw new CommandLineException("Failed to load background image: " + cmd.getOptionValue("b"));
            }
        }

        if(background == null) {
            try {
                InputStream in = VideoConfig.class.getResourceAsStream("/images/oak_background.png");
                if (in == null) {
                    throw new IllegalStateException("Resource not found: /images/oak_background.png");
                }

                Path tempFile = Files.createTempFile("oak-background-", ".png");
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                in.close();

                background = tempFile.toFile();
            } catch (IOException e) {
                throw new ImageProcessingException("Default background resource not found: /images/oak_background.png");
            }
        }

        if(cmd.hasOption("a")) {
            if(Validator.isValidFile(cmd.getOptionValue("a"))) {
                audioFilePath = cmd.getOptionValue("a");
                audioFromMidi = false;
            }
            else {
                throw new CommandLineException("Audio file does not exist: " + cmd.getOptionValue("a"));
            }
        }
        else {
            LOG.info("No audio file passed, creating audio from midi file");
            audioFromMidi = true;
            audioFilePath = "";
        }
    }

    public boolean isOutputPdf() {
        return outputPdf;
    }

    public boolean isOutputVid() {
        return outputVid;
    }

    public int getFramerate() {
        return framerate;
    }

    public double getAudioOffset() {
        return audioOffset;
    }

    public Color getNoteOnColor() {
        return noteOnColor;
    }

    public Color getNoteOffColor() {
        return noteOffColor;
    }

    public Color getPreviewNoteColor() {
        return previewNoteColor;
    }

    public int getDims() {
        return dims;
    }

    public String getMidiFilePath() {
        return midiFilePath;
    }

    public File getBackground() {
        return background;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public boolean isAudioFromMidi() { // todo remove?
        return audioFromMidi;
    }
}
