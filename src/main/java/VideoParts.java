//import midi.MidiFile;
//import midi.MidiNote;
//import midi.MidiToWavRenderer;
//
//import javax.sound.midi.InvalidMidiDataException;
//import javax.sound.midi.MidiUnavailableException;
//import javax.sound.midi.Sequence;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//
//public class VideoParts {
//
//    //================================================================================
//    // Properties
//    //================================================================================
//
//    private final MidiFile midiFile;
//    private final Sequence sequence;
//    private final ArrayList<MidiNote> midiNotes;
//    private final ArrayList<MidiNote> offNotes;
//    private final File background;
//    private final File audio;
//    private final String outputFilePath;
//
//    //================================================================================
//    // Constructors
//    //================================================================================
//
//    public VideoParts(MidiFile midiFile, Sequence sequence, ArrayList<MidiNote> midiNotes, ArrayList<MidiNote> offNotes, File background, String outputFilePath) {
//        this.midiFile = midiFile;
//        this.sequence = sequence;
//        this.midiNotes = midiNotes;
//        this.offNotes = offNotes;
//        this.background = background;
//        this.audio = audioFromMidi();
//        this.outputFilePath = outputFilePath;
//    }
//
//    public VideoParts(MidiFile midiFile, Sequence sequence, ArrayList<MidiNote> midiNotes, ArrayList<MidiNote> offNotes, File background, File audio, String outputFilePath) {
//        this.midiFile = midiFile;
//        this.sequence = sequence;
//        this.midiNotes = midiNotes;
//        this.offNotes = offNotes;
//        this.background = background;
//        this.audio = audio;
//        this.outputFilePath = outputFilePath;
//    }
//
//    public File audioFromMidi() {
//        try {
//            String tempAudioPath = "src/main/resources/temp/temp.wav";
//            MidiToWavRenderer wavRenderer = new MidiToWavRenderer();
//            wavRenderer.createWavFile(
//                    new File("src/main/resources/sounds/ocarina.sf2"),
//                    sequence,
//                    new File(tempAudioPath));
//            return new File(tempAudioPath);
//        }
//        catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
//            throw new AudioRenderingException("Unable to render audio from midi");
//        }
//    }
//
//    //================================================================================
//    // Accessors
//    //================================================================================
//
//    public MidiFile getMidiFile() {
//        return midiFile;
//    }
//
//    public Sequence getSequence() {
//        return sequence;
//    }
//
//    public ArrayList<MidiNote> getMidiNotes() {
//        return midiNotes;
//    }
//
//    public ArrayList<MidiNote> getOffNotes() {
//        return offNotes;
//    }
//
//    public File getBackground() {
//        return background;
//    }
//
//    public File getAudio() {
//        return audio;
//    }
//
//    public String getOutputFilePath() {
//        return outputFilePath;
//    }
//}
