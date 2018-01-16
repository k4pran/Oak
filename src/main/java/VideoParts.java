import midi.MidiFile;

import java.io.File;
import java.util.ArrayList;

public class VideoParts {

    //================================================================================
    // Properties
    //================================================================================

    private final MidiFile midiFile;
    private final File background;
    private final File audio;
    private final ArrayList<File> introFrames;
    private final ArrayList<File> outroFrames;
    private final String outputFilePath;

    //================================================================================
    // Constructors
    //================================================================================

    public VideoParts(MidiFile midiFile, File background, File audio, String outputFilePath) {
        this.midiFile = midiFile;
        this.background = background;
        this.audio = audio;
        this.introFrames = new ArrayList<>();
        this.outroFrames = new ArrayList<>();
        this.outputFilePath = outputFilePath;
    }

    public VideoParts(MidiFile midiFile, File background, File audio, ArrayList<File> introFrames,
                      ArrayList<File> outroFrames, String outputFilePath) {
        this.midiFile = midiFile;
        this.background = background;
        this.audio = audio;
        this.introFrames = introFrames;
        this.outroFrames = outroFrames;
        this.outputFilePath = outputFilePath;
    }

    //================================================================================
    // Accessors
    //================================================================================

    public MidiFile getMidiFile() {
        return midiFile;
    }

    public File getBackground() {
        return background;
    }

    public File getAudio() {
        return audio;
    }

    public ArrayList<File> getIntroFrames() {
        return introFrames;
    }

    public ArrayList<File> getOutroFrames() {
        return outroFrames;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}
