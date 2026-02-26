package io.ryanjames.oak.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

public final class MidiCopyUtils {

    private MidiCopyUtils() {}

    /** Creates a new Sequence containing exactly one Track: a copy of srcTrack. */
    public static Sequence singleTrackCopy(Sequence input, Track srcTrack) throws InvalidMidiDataException {
        if (input == null) throw new NullPointerException("input");
        if (srcTrack == null) throw new NullPointerException("srcTrack");

        // NOTE: Sequence ctor with numTracks already creates tracks, so don't createTrack() again.
        Sequence out = new Sequence(input.getDivisionType(), input.getResolution(), 1);
        Track outTrack = out.getTracks()[0];

        for (int i = 0; i < srcTrack.size(); i++) {
            MidiEvent ev = srcTrack.get(i);
            MidiMessage msgCopy = deepCopyMessage(ev.getMessage());
            outTrack.add(new MidiEvent(msgCopy, ev.getTick()));
        }

        // Ensure End-of-Track is present at end (some files have it; some don't)
        ensureEndOfTrack(outTrack);

        return out;
    }

    public static MidiMessage deepCopyMessage(MidiMessage msg) throws InvalidMidiDataException {
        if (msg instanceof ShortMessage sm) {
            ShortMessage copy = new ShortMessage();
            copy.setMessage(sm.getCommand(), sm.getChannel(), sm.getData1(), sm.getData2());
            return copy;
        }
        if (msg instanceof MetaMessage mm) {
            MetaMessage copy = new MetaMessage();
            byte[] data = mm.getData();
            copy.setMessage(mm.getType(), data, data.length);
            return copy;
        }
        if (msg instanceof SysexMessage sx) {
            SysexMessage copy = new SysexMessage();
            byte[] data = sx.getData();
            // SysexMessage wants the full message including status, so use getMessage()
            byte[] full = sx.getMessage();
            copy.setMessage(full, full.length);
            return copy;
        }

        // Generic fallback (rare in practice):
        // clone the raw bytes into an "unknown" MidiMessage
        byte[] raw = msg.getMessage();
        return new MidiMessage(raw.clone()) {
            @Override public Object clone() { return this; }
        };
    }

    public static void ensureEndOfTrack(Track track) throws InvalidMidiDataException {
        long lastTick = 0;
        boolean hasEot = false;

        for (int i = 0; i < track.size(); i++) {
            MidiEvent ev = track.get(i);
            lastTick = Math.max(lastTick, ev.getTick());
            MidiMessage msg = ev.getMessage();
            if (msg instanceof MetaMessage meta && meta.getType() == 0x2F) {
                hasEot = true;
            }
        }

        if (!hasEot) {
            MetaMessage eot = new MetaMessage();
            eot.setMessage(0x2F, new byte[0], 0);
            track.add(new MidiEvent(eot, lastTick));
        }
    }
}