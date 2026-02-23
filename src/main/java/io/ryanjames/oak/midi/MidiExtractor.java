package io.ryanjames.oak.midi;

import javax.sound.midi.Sequence;

/**
 * Extracts a transformed MIDI sequence (e.g., melody-only).
 */
public interface MidiExtractor {
    /**
     * @param input source MIDI sequence
     * @return new MIDI sequence containing extracted content
     */
    Sequence extract(Sequence input);
}
