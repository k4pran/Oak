package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.util.Objects;

/**
 * For each track: detect the first MIDI channel used by any ShortMessage,
 * then copy only events on that channel (plus all Meta/SysEx) into a new sequence.
 *
 * If a track has no ShortMessages at all, it is copied as-is (Meta/SysEx only).
 */
public final class FirstChannelTakerTransformer implements SequenceTransformer {

    @Override
    public Sequence transform(Sequence input) {
        Objects.requireNonNull(input, "input");

        try {
            Sequence out = new Sequence(input.getDivisionType(), input.getResolution(), input.getTracks().length);

            Track[] inTracks = input.getTracks();
            Track[] outTracks = out.getTracks(); // pre-created by ctor when numTracks > 0

            for (int ti = 0; ti < inTracks.length; ti++) {
                Track inTrack = inTracks[ti];
                Track outTrack = outTracks[ti];

                Integer channelToKeep = 0;

                for (int i = 0; i < inTrack.size(); i++) {
                    MidiEvent ev = inTrack.get(i);
                    MidiMessage msg = ev.getMessage();

                    if (msg instanceof ShortMessage sm) {
                        // Keep only chosen channel (if any)
                        if (sm.getChannel() == channelToKeep) {
                            outTrack.add(new MidiEvent(MidiCopyUtils.deepCopyMessage(msg), ev.getTick()));
                        }
                    } else {
                        // Keep Meta + SysEx etc always
                        outTrack.add(new MidiEvent(MidiCopyUtils.deepCopyMessage(msg), ev.getTick()));
                    }
                }

                // Ensure EOT is present and at the end
                MidiCopyUtils.ensureEndOfTrack(outTrack);
            }

            return out;
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException("Failed to construct transformed sequence", e);
        }
    }
}