package io.ryanjames.oak.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

/**
 * Transforms a MIDI sequence (e.g., melody-only).
 */
public interface SequenceTransformer {
    /**
     * @param input source MIDI sequence
     * @return the transformed sequence
     */
    Sequence transform(Sequence input) throws InvalidMidiDataException;
}
