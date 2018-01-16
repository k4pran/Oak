import midi.MidiNotes;
import midi.NoteProcessingException;
import text.CustomText;
import video.FFMpeg;

import java.awt.image.BufferedImage;
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
        CmdParser parser = new CmdParser(args);

        if(parser.cmd.hasOption("gui")) {
            //Todo
        }
        else {
            parser.loadFromCmdLine();
        }

        // Get image sprites
        ArrayList<Integer> notes = MidiNotes.extractNotes(videoParts.getMidiFile());
        ocarinaSprites = MidiNotes.mapNotesToImages(notes);

        // Construct image frame
        ImageFactory imageFactory = new ImageFactory(settings, videoParts);
        imageFrames = imageFactory.createImages(ocarinaSprites);

        // Extract note lengths
        ArrayList<Double> frameDurations = new ArrayList<>(Collections.nCopies(videoParts.getIntroFrames().size(), 1000.00));
        frameDurations.addAll(Collections.nCopies(CustomText.getIntroText().length, 1000.00));
        frameDurations.addAll(MidiNotes.extractNoteLengths(videoParts.getMidiFile()));
        frameDurations.addAll(Collections.nCopies(CustomText.getOutroText().length, 1000.00));
        frameDurations.addAll(Collections.nCopies(videoParts.getOutroFrames().size() + 1, 1000.00));

        // Video frames
        int introCount = videoParts.getIntroFrames().size() + CustomText.getIntroText().length;
        int outroCount = videoParts.getOutroFrames().size() + CustomText.getOutroText().length;
        ArrayList<BufferedImage> videoFrames = VideoFactory.getVideoFrames(imageFrames, frameDurations, settings.getFramerate(), introCount, outroCount);

        // Output video
        FFMpeg ffMpeg = new FFMpeg();
        Double audioOffset = (settings.getAudioOffset() / 1000) + introCount;
        ffMpeg.outputTutorial(videoFrames ,videoParts.getOutputFilePath(), videoParts.getAudio().getAbsolutePath(),
                settings.getFramerate(), audioOffset);
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
