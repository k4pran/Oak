package io.ryanjames.oak.config;

import io.ryanjames.oak.CommandLineException;
import io.ryanjames.oak.VideoConfig;
import io.ryanjames.oak.color.ColorConversions;
import io.ryanjames.oak.validation.Validator;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoArgParser {

    private static final Logger LOG = LoggerFactory.getLogger(VideoArgParser.class);

    private static final int MIN_FPS = 60;
    private static final int MAX_FPS = 144;

    private VideoArgParser() {}

    public static VideoConfig fromCmd(CommandLine cmd) throws CommandLineException {
        VideoArgParser parser = new VideoArgParser();
        return parser.load(cmd);
    }

    private VideoConfig load(CommandLine cmd) throws CommandLineException {
        VideoConfig videoConfig = new VideoConfig();

        videoConfig.setOutputPdf(cmd.hasOption("pdf"));
        videoConfig.setOutputVid(cmd.hasOption("vid"));
        if (!videoConfig.isOutputPdf() && !videoConfig.isOutputVid()) {
            LOG.info("pdf and vid options not specified. Defaulting to both enabled.");
            videoConfig.setOutputPdf(true);
            videoConfig.setOutputVid(true);
        }

        if(cmd.hasOption("d")) {
            try {
                videoConfig.setDims(Integer.parseInt(cmd.getOptionValue("d")));
            }
            catch (NumberFormatException e) {
                LOG.warn("Invalid dimensions, defaulting to 3 x 3");
            }
        }

        if(cmd.hasOption("c")) {
            videoConfig.setNoteOnColor(ColorConversions.interrogateColor(cmd.getOptionValue("c")));
        }
        if(cmd.hasOption("co")) {
            videoConfig.setNoteOffColor(ColorConversions.interrogateColor(cmd.getOptionValue("co")));
        }
        if(cmd.hasOption("cp")) {
            videoConfig.setPreviewNoteColor(ColorConversions.interrogateColor(cmd.getOptionValue("cp")));
        }

        if(cmd.hasOption("fr")) {
            try {
                int fps = Integer.parseInt(cmd.getOptionValue("fr"));
                if(fps > MIN_FPS && fps <= MAX_FPS) {
                    videoConfig.setFramerate(fps);
                }
                else {
                    LOG.warn("Invalid fps. Allowed values range: {}-{}. Defaulting to {}", MIN_FPS, MAX_FPS, videoConfig.getFramerate());
                }
            }
            catch(NumberFormatException e) {
                LOG.warn("Invalid fps. Must be numerical value of number of frames per second, defaulting to {}", videoConfig.getFramerate());
            }
        }

        if(cmd.hasOption("ofs")) {
            try {
                videoConfig.setAudioOffset(Double.parseDouble(cmd.getOptionValue("ofs")));
            }
            catch(NumberFormatException e) {
                LOG.warn("Invalid offset. Must be a numerical offset value in seconds. Defaulting to no offset");
            }
        }

        if(cmd.hasOption("i")) {
            videoConfig.setMidiFilePath(cmd.getOptionValue("i"));
        }

        if(cmd.hasOption("o")) {
            String path = cmd.getOptionValue("o");
            if(Validator.isValidPath(path)) {
                videoConfig.setOutputDir(path);
            }
            else {
                throw new CommandLineException("Output file path is not valid: " + cmd.getOptionValue("o"));
            }
        }

//        if(cmd.hasOption("b")) { // todo needs to be separate from global configs
//            if(Validator.isValidImageFile(cmd.getOptionValue("b"))) {
//                videoConfig.setBackground(new File(cmd.getOptionValue("b")));
//            }
//            else {
//                throw new CommandLineException("Failed to load background image: " + cmd.getOptionValue("b"));
//            }
//        }
//
//        if(videoConfig.getBackground() == null) {
//            try {
//                InputStream in = VideoConfig.class.getResourceAsStream("/images/oak_background.png");
//                if (in == null) {
//                    throw new IllegalStateException("Resource not found: /images/oak_background.png");
//                }
//
//                Path tempFile = Files.createTempFile("oak-background-", ".png");
//                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
//                in.close();
//
//                videoConfig.setBackground(tempFile.toFile());
//            } catch (IOException e) {
//                throw new ImageProcessingException("Default background resource not found: /images/oak_background.png");
//            }
//        }

        if(cmd.hasOption("a")) {
            if(Validator.isValidFile(cmd.getOptionValue("a"))) {
                videoConfig.setAudioFilePath(cmd.getOptionValue("a"));
                videoConfig.setAudioFromMidi(false);
            }
            else {
                throw new CommandLineException("Audio file does not exist: " + cmd.getOptionValue("a"));
            }
        }
        else {
            LOG.info("No audio file passed, creating audio from midi file");
            videoConfig.setAudioFromMidi(true);
            videoConfig.setAudioFilePath("");
        }
        return videoConfig;
    }
}