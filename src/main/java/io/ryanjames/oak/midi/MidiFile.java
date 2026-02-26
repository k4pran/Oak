package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

/**
 * Immutable MIDI file metadata + sequence container.
 */
public final class MidiFile {

    //================================================================================
    // Properties (pure data)
    //================================================================================

    private final File file;
    private final Sequence sequence;
    private final int resolution;
    private final double tempoBpm;
    private final double ticksInMs;

    //================================================================================
    // Private constructor (only Builder can create)
    //================================================================================

    private MidiFile(File file,
                     Sequence sequence,
                     int resolution,
                     double tempoBpm,
                     double ticksInMs) {
        this.file = file;
        this.sequence = sequence;
        this.resolution = resolution;
        this.tempoBpm = tempoBpm;
        this.ticksInMs = ticksInMs;
    }

    //================================================================================
    // Accessors
    //================================================================================

    public File getFile() {
        return file;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public int getResolution() {
        return resolution;
    }

    public double getTempoBpm() {
        return tempoBpm;
    }

    public double getTicksInMs() {
        return ticksInMs;
    }

    //================================================================================
    // Static Builder / Loader
    //================================================================================

    public static final class Loader {

        private Loader() {}

        public static MidiFile load(File midiFile) throws MidiFileLoaderException {
            try {
                Sequence sequence = MidiSystem.getSequence(midiFile);

                int resolution = sequence.getResolution();
                double tempo = extractTempo(sequence);
                double ticksInMs = 60000.0 / (tempo * resolution);

                return new MidiFile(
                        midiFile,
                        sequence,
                        resolution,
                        tempo,
                        ticksInMs
                );

            } catch (IOException | InvalidMidiDataException e) {
                throw new MidiFileLoaderException("Unable to load MIDI file", e);
            }
        }

        /**
         * Extract tempo from first tempo meta event (0x51).
         * Falls back to 120 BPM if none found.
         */
        private static double extractTempo(Sequence sequence) {
            for (Track track : sequence.getTracks()) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();
                    if (message instanceof MetaMessage meta) {
                        if (meta.getType() == 0x51) {
                            byte[] data = meta.getData();
                            int mpq = ((data[0] & 0xFF) << 16)
                                    | ((data[1] & 0xFF) << 8)
                                    | (data[2] & 0xFF);
                            return 60_000_000.0 / mpq;
                        }
                    }
                }
            }

            // Default MIDI tempo if none specified
            return 120.0;
        }
    }
}