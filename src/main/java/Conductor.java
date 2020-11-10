import midi.NoteExtractor;
import midi.NoteProcessingException;
import midi.NoteToImage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import video.FFMpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

/**
 * Controls the flow of the program.
 */

public class Conductor {

    private static final Logger LOG = LoggerFactory.getLogger(Conductor.class);

    private static final double SECOND_AS_MS = 1000.;

    private static VideoConfig videoConfig;
    private static TextConfig textConfig;
    private static ArrayList<BufferedImage> imageFrames;

    public static void start(String[] args) throws CommandLineException, NoteProcessingException, FrameConstructionException, ParseException {

        boolean isLoaded = parseCommandLine(args);
        if (!isLoaded) {
            return;
        }

        createImageFrames();

        ArrayList<Double> frameDurations = getFrameDurations();

        ArrayList<BufferedImage> videoFrames = createVideoFrames(frameDurations);

        outputVideo(videoFrames, frameDurations);

        cleanUp();
    }

    private static boolean parseCommandLine(String[] args) throws CommandLineException, ParseException {
        Optional<CommandLine> parser = CmdParser.parseCmdLine(args);
        if (parser.isPresent()) {
            videoConfig = CmdParser.loadVideoConfig(parser.get());
            textConfig = CmdParser.loadTextConfig(parser.get());
            return true;
        }
        return false;
    }

    private static void createImageFrames() {
        // Get image sprites
        ArrayList<BufferedImage> ocarinaSprites = NoteToImage.mapNotesToImages(videoConfig.getMidiNotes());

        // Construct image frame
        ImageFactory imageFactory = new ImageFactory(videoConfig);
        imageFrames = imageFactory.createImages(ocarinaSprites);
    }

    private static ArrayList<Double> getFrameDurations() {
        ArrayList<Double> frameDurations = new ArrayList<>();
        // Intro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getIntroText().size(),
                SECOND_AS_MS * videoConfig.getMidiFile().getTicksInMs()));

        // Played note frame durations
        frameDurations.addAll(NoteExtractor.extractDurations(
                videoConfig.getMidiNotes(), videoConfig.getOffNotes(), videoConfig.getMidiFile().getTicksInMs()));

        // Outro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getOutroText().size(),
                SECOND_AS_MS * videoConfig.getMidiFile().getTicksInMs()));
        return frameDurations;
    }

    private static ArrayList<BufferedImage> createVideoFrames(
            ArrayList<Double> frameDurations) {
        return VideoFactory.getVideoFrames(imageFrames, frameDurations, videoConfig.getFramerate());
    }

    private static void outputVideo(ArrayList<BufferedImage> videoFrames, ArrayList<Double> frameDurations) {
        FFMpeg ffMpeg = new FFMpeg();
        double audioOffset = videoConfig.getAudioOffset();
        for(int i = 0; i < CustomText.getIntroText().size(); i++) {
            audioOffset += frameDurations.get(i) / SECOND_AS_MS;
        }
        ffMpeg.outputTutorial(videoFrames, videoConfig.getOutputFilePath(), videoConfig.getAudio().getAbsolutePath(),
                videoConfig.getFramerate(), audioOffset);
    }

    private static void cleanUp() {
        String wavPath = "src/main/resources/temp/temp.wav";
        File file = new File(wavPath);
        if(file.delete()) {
            LOG.info("Temporary audio file {} deleted", wavPath);
        }
    }
}
