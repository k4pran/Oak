package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.util.Objects;

/**
 * Extracts the pitch range (lowest and highest NOTE_ON pitch) from a MIDI sequence.
 */
public final class NoteRangeExtractor {

    private final Integer channelFilter; // null = all channels

    public NoteRangeExtractor() {
        this(null);
    }

    public NoteRangeExtractor(Integer channelFilter) {
        this.channelFilter = channelFilter;
    }

    public NoteRange extract(Sequence input) {
        Objects.requireNonNull(input, "input");

        int lowest = 127;
        int highest = 0;
        boolean found = false;

        for (Track track : input.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiMessage msg = track.get(i).getMessage();

                if (!(msg instanceof ShortMessage sm)) continue;

                if (sm.getCommand() != ShortMessage.NOTE_ON) continue;
                if (sm.getData2() == 0) continue; // velocity 0 = NOTE_OFF

                if (channelFilter != null && sm.getChannel() != channelFilter) continue;

                int pitch = sm.getData1();

                if (pitch < lowest) lowest = pitch;
                if (pitch > highest) highest = pitch;

                found = true;
            }
        }

        if (!found) {
            return new NoteRange(-1, -1);
        }

        return new NoteRange(lowest, highest);
    }

    /**
     * Immutable result.
     */
    public static final class NoteRange {
        private final int lowest;
        private final int highest;

        public NoteRange(int lowest, int highest) {
            this.lowest = lowest;
            this.highest = highest;
        }

        public int lowest() {
            return lowest;
        }

        public int highest() {
            return highest;
        }

        public int span() {
            return (lowest >= 0 && highest >= 0) ? highest - lowest : 0;
        }

        public boolean isValid() {
            return lowest >= 0 && highest >= 0;
        }

        @Override
        public String toString() {
            return "NoteRange[" + lowest + "–" + highest + "]";
        }
    }
}