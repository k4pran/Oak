package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.util.Objects;

/**
 * Returns a new Sequence containing only track 0 from the input.
 * Copies all events (Meta/SysEx/ShortMessage) and ensures End-of-Track.
 */
public final class FirstTrackOnlyTransformer implements SequenceTransformer {

    @Override
    public Sequence transform(Sequence input) {
        Objects.requireNonNull(input, "input");

        Track[] inTracks = input.getTracks();
        if (inTracks.length == 0) {
            try {
                return new Sequence(input.getDivisionType(), input.getResolution(), 1);
            } catch (InvalidMidiDataException e) {
                throw new IllegalStateException("Failed to create empty output sequence", e);
            }
        }

        try {
            Track first = inTracks[0];

            // Create output with exactly 1 track (already created by ctor)
            Sequence out = new Sequence(input.getDivisionType(), input.getResolution(), 1);
            Track outTrack = out.getTracks()[0];

            for (int i = 0; i < first.size(); i++) {
                MidiEvent ev = first.get(i);
                MidiMessage msgCopy = MidiCopyUtils.deepCopyMessage(ev.getMessage());
                outTrack.add(new MidiEvent(msgCopy, ev.getTick()));
            }

            MidiCopyUtils.ensureEndOfTrack(outTrack);
            return out;

        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException("Failed to construct transformed sequence", e);
        }
    }
}