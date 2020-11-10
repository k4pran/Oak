import color.ColorConversions;
import midi.*;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import validation.Validator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VideoConfig {

    private static final Logger LOG = LoggerFactory.getLogger(VideoConfig.class);

    private static final int DEFAULT_DIMS = 3;
    private static final int MIN_FPS = 60;
    private static final int MAX_FPS = 144;
    private static final int DEFAULT_FPS = 60;
    private static final boolean AUDIO_FROM_MIDI_DEFAULT = true;

    private boolean outputPdf;
    private boolean outputVid;
    private int framerate;
    private double audioOffset;
    private Color noteOnColor;
    private Color noteOffColor;
    private Color previewNoteColor;
    private int dims;
    private MidiFile midiFile;
    private Sequence sequence;
    private ArrayList<MidiNote> midiNotes;
    private ArrayList<MidiNote> offNotes;
    private File background;
    private File audio;
    private String outputFilePath;
    private boolean audioFromMidi;

    private final CommandLine cmd;

    public VideoConfig(CommandLine cmd) throws CommandLineException {
        this.cmd = cmd;
        framerate = DEFAULT_FPS;
        dims = DEFAULT_DIMS;
        audioFromMidi = AUDIO_FROM_MIDI_DEFAULT;
        noteOffColor = Color.BLUE;
        noteOnColor = Color.RED;
        previewNoteColor = Color.GREEN;

        this.load();
    }

    private void load() throws CommandLineException {

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
                    LOG.warn("Invalid fps. Allowed values range: {}-{}. Defaulting to {}", MIN_FPS, MAX_FPS, DEFAULT_FPS);
                    framerate = DEFAULT_FPS;
                }
            }
            catch(NumberFormatException e) {
                LOG.warn("Invalid fps. Must be numerical value of number of frames per second, defaulting to {}", DEFAULT_FPS);
                framerate = DEFAULT_FPS;
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
            String midiPath = cmd.getOptionValue("i");
            loadMidiInput(midiPath);
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

        if(cmd.hasOption("a")) {
            if(Validator.isValidFile(cmd.getOptionValue("a"))) {
                audio = new File(cmd.getOptionValue("a"));
                audioFromMidi = false;
            }
            else {
                throw new CommandLineException("Audio file does not exist: " + cmd.getOptionValue("a"));
            }
        }
        else {
            LOG.info("No audio file passed, creating audio from midi file");
            audio = audioFromMidi();
        }
    }

    private void loadMidiInput(String midiPath) throws CommandLineException {
        try {
            midiFile = new MidiFile(new File(midiPath));
            this.sequence = midiFile.getSequence();

            MidiCropper midiCropper = new MidiCropper(sequence);
            sequence = midiCropper.cropDuplicates();
            NoteExtractor noteExtractor = new NoteExtractor(sequence);
            noteExtractor.renderSequence();

            noteExtractor.transposeSequence();
            this.midiNotes = noteExtractor.simpleToMidiNotes(noteExtractor.getSimpleOnNotes());
            this.offNotes = noteExtractor.simpleToMidiNotes(noteExtractor.getSimpleOffNotes());
            this.sequence = noteExtractor.getSequence();
        }
        catch (MidiFileLoaderException e) {
            e.printStackTrace();
            throw new CommandLineException("Failed to load midi file");
        }
    }

    public File audioFromMidi() {
        try {
            String tempAudioDir = "src/main/resources/temp/";
            boolean dirExists = mkDir(tempAudioDir);

            if (!dirExists) {
                throw new CommandLineException("Failed to create temporary audio file directory at " + tempAudioDir);
            }

            String tempAudioPath = tempAudioDir + "temp.wav";
            MidiToWavRenderer wavRenderer = new MidiToWavRenderer();
            wavRenderer.createWavFile(
                    new File("src/main/resources/sounds/ocarina.sf2"),
                    sequence,
                    new File(tempAudioPath));
            return new File(tempAudioPath);
        }
        catch (MidiUnavailableException | InvalidMidiDataException | IOException | CommandLineException e) {
            throw new AudioRenderingException("Unable to render audio from midi");
        }
    }

    private boolean mkDir(String dir) {
        File targetDir = new File(dir);
        if (!targetDir.exists() || !targetDir.isDirectory()) {
            return targetDir.mkdir();
        }
        return true;
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

    public MidiFile getMidiFile() {
        return midiFile;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public ArrayList<MidiNote> getMidiNotes() {
        return midiNotes;
    }

    public ArrayList<MidiNote> getOffNotes() {
        return offNotes;
    }

    public File getBackground() {
        return background;
    }

    public File getAudio() {
        return audio;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public boolean isAudioFromMidi() {
        return audioFromMidi;
    }
}
