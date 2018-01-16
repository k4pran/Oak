package midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

/**
 * Contains midi file along with useful midi information for convenient access.
 */

public class MidiFile {

    //================================================================================
    // Properties
    //================================================================================

    private File midiFile;
    private Sequence sequence;
    private int resolution;
    private Double tempo;
    private Double ticksInMs;

    //================================================================================
    // Constructor
    //================================================================================

    public MidiFile(File midiFile) throws MidiFileLoaderException {
        try {
            this.midiFile = midiFile;
            this.sequence = MidiSystem.getSequence(midiFile);
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(sequence);
            this.resolution = sequence.getResolution();
            this.tempo = new Double(sequencer.getTempoInBPM());
            this.ticksInMs = 60000 / (tempo * resolution);
        }
        catch(IOException | MidiUnavailableException | InvalidMidiDataException e) {
            throw new MidiFileLoaderException("Unable to load midi file");
        }
    }

    //================================================================================
    // Accessors
    //================================================================================

    public File getFile() {
        return midiFile;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public int getResolution() {
        return resolution;
    }

    public Double getTempo() {
        return tempo;
    }

    public Double getTicksInMs() {
        return ticksInMs;
    }
}
