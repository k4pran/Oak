import javafx.application.Application;
import midi.NoteExtractor;
import midi.NoteProcessingException;
import midi.NoteToImage;
import video.FFMpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Controls the flow of the program.
 */

public class Conductor {

    //================================================================================
    // Properties
    //================================================================================

    private static Settings settings;
    private static VideoParts videoParts;
    private static ArrayList<BufferedImage> ocarinaSprites;
    private static ArrayList<BufferedImage> imageFrames;

    //================================================================================
    // General methods
    //================================================================================

    public static void start(String[] args) throws CommandLineException, NoteProcessingException, FrameConstructionException {

        parseCommandLine(args);

        createImageFrames();

        ArrayList<Double> frameDurations = getFrameDurations();

        ArrayList<BufferedImage> videoFrames = createVideoFrames(frameDurations);

        outputVideo(videoFrames, frameDurations);

        cleanUp();
    }

    private static void parseCommandLine(String[] args) throws CommandLineException {
        CmdParser parser = new CmdParser(args);
        parser.loadFromCmdLine();
    }

    private static void createImageFrames() {
        // Get image sprites
        ocarinaSprites = NoteToImage.mapNotesToImages(videoParts.getMidiNotes());

        // Construct image frame
        ImageFactory imageFactory = new ImageFactory(settings, videoParts);
        imageFrames = imageFactory.createImages(ocarinaSprites);
    }

    private static ArrayList<Double> getFrameDurations() {
        ArrayList<Double> frameDurations = new ArrayList<>();
        // Intro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getIntroText().size(),
                1000.00 * videoParts.getMidiFile().getTicksInMs()));

        // Played note frame durations
        frameDurations.addAll(NoteExtractor.extractDurations(
                videoParts.getMidiNotes(), videoParts.getOffNotes(), videoParts.getMidiFile().getTicksInMs()));

        // Outro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getOutroText().size(),
                1000.00 * videoParts.getMidiFile().getTicksInMs()));
        return frameDurations;
    }

    private static ArrayList<BufferedImage> createVideoFrames(
            ArrayList<Double> frameDurations) {
        return VideoFactory.getVideoFrames(imageFrames, frameDurations, settings.getFramerate());
    }

    private static void outputVideo(ArrayList<BufferedImage> videoFrames, ArrayList<Double> frameDurations) {
        FFMpeg ffMpeg = new FFMpeg();
        Double audioOffset = settings.getAudioOffset();
        for(int i = 0; i < CustomText.getIntroText().size(); i++) {
            audioOffset += frameDurations.get(i) / 1000;
        }
        ffMpeg.outputTutorial(videoFrames ,videoParts.getOutputFilePath(), videoParts.getAudio().getAbsolutePath(),
                settings.getFramerate(), audioOffset);
    }

    private static void cleanUp() {
        File file = new File("src/main/resources/temp/temp.wav");
        if(file.delete()) {
            System.out.println("Temporary audio file deleted");
        }
    }

    //================================================================================
    // Accessors
    //================================================================================

    public static void setSettings(Settings settings) {
        Conductor.settings = settings;
    }

    public static void setVideoParts(VideoParts videoParts) {
        Conductor.videoParts = videoParts;
    }
}
