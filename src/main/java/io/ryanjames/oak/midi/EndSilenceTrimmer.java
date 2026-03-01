package io.ryanjames.oak.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Trims trailing silence by limiting events after the last note-off.
 *
 * Allowed silence is expressed in milliseconds. A negative value disables trimming.
 */
public final class EndSilenceTrimmer implements SequenceTransformer {

    private final double allowedSilenceMs;

    public EndSilenceTrimmer(double allowedSilenceMs) {
        this.allowedSilenceMs = allowedSilenceMs;
    }

    @Override
    public Sequence transform(Sequence input) throws InvalidMidiDataException {
        Objects.requireNonNull(input, "input");

        if (allowedSilenceMs < 0.0) {
            return input;
        }
        if (input.getDivisionType() != Sequence.PPQ) {
            return input;
        }

        long lastNoteTick = findLastNoteTick(input);
        if (lastNoteTick < 0) {
            return input;
        }

        long extraTicks = ticksForMs(input, lastNoteTick, allowedSilenceMs);
        long cutoffTick = lastNoteTick + Math.max(0L, extraTicks);

        Track[] inTracks = input.getTracks();
        Sequence out = new Sequence(input.getDivisionType(), input.getResolution(), inTracks.length);

        for (int ti = 0; ti < inTracks.length; ti++) {
            Track in = inTracks[ti];
            Track outTrack = out.getTracks()[ti];
            int[][] activeNotes = new int[16][128];

            for (int i = 0; i < in.size(); i++) {
                MidiEvent ev = in.get(i);
                long tick = ev.getTick();
                if (tick > cutoffTick) {
                    break;
                }

                MidiMessage msg = ev.getMessage();
                if (msg instanceof MetaMessage meta && meta.getType() == 0x2F) {
                    continue; // we'll add EOT at cutoff
                }

                if (msg instanceof ShortMessage sm) {
                    int cmd = sm.getCommand();
                    int ch = sm.getChannel();
                    int pitch = sm.getData1();
                    int vel = sm.getData2();

                    boolean isOn = (cmd == ShortMessage.NOTE_ON) && vel > 0;
                    boolean isOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && vel == 0);

                    if (isOn && pitch >= 0 && pitch <= 127) {
                        activeNotes[ch][pitch]++;
                    } else if (isOff && pitch >= 0 && pitch <= 127) {
                        if (activeNotes[ch][pitch] > 0) {
                            activeNotes[ch][pitch]--;
                        }
                    }
                }

                outTrack.add(new MidiEvent(MidiCopyUtils.deepCopyMessage(msg), tick));
            }

            addNoteOffsForActive(outTrack, activeNotes, cutoffTick);
            addEndOfTrack(outTrack, cutoffTick);
        }

        return out;
    }

    private static long findLastNoteTick(Sequence sequence) {
        long lastTick = -1;
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent ev = track.get(i);
                MidiMessage msg = ev.getMessage();
                if (!(msg instanceof ShortMessage sm)) continue;

                int cmd = sm.getCommand();
                int vel = sm.getData2();

                boolean isOn = (cmd == ShortMessage.NOTE_ON) && vel > 0;
                boolean isOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && vel == 0);

                if (isOn || isOff) {
                    lastTick = Math.max(lastTick, ev.getTick());
                }
            }
        }
        return lastTick;
    }

    private static void addNoteOffsForActive(Track outTrack, int[][] activeNotes, long cutoffTick)
            throws InvalidMidiDataException {
        for (int ch = 0; ch < activeNotes.length; ch++) {
            for (int pitch = 0; pitch < activeNotes[ch].length; pitch++) {
                int count = activeNotes[ch][pitch];
                for (int i = 0; i < count; i++) {
                    ShortMessage off = new ShortMessage();
                    off.setMessage(ShortMessage.NOTE_OFF, ch, pitch, 0);
                    outTrack.add(new MidiEvent(off, cutoffTick));
                }
            }
        }
    }

    private static void addEndOfTrack(Track outTrack, long cutoffTick) throws InvalidMidiDataException {
        MetaMessage eot = new MetaMessage();
        eot.setMessage(0x2F, new byte[0], 0);
        outTrack.add(new MidiEvent(eot, cutoffTick));
    }

    private static long ticksForMs(Sequence sequence, long startTick, double ms) {
        if (ms <= 0.0) {
            return 0L;
        }

        int resolution = sequence.getResolution();
        List<TempoPoint> tempo = buildTempoMap(sequence);

        int idx = findTempoIndexAtOrBefore(tempo, startTick);
        long curTick = startTick;
        double remainingMs = ms;
        long ticksAdded = 0L;

        while (remainingMs > 0.0) {
            TempoPoint tp = tempo.get(idx);
            long nextTick = (idx + 1 < tempo.size()) ? tempo.get(idx + 1).tick : Long.MAX_VALUE;
            long segmentTicks = nextTick == Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(0L, nextTick - curTick);
            double segmentMs = segmentTicks == Long.MAX_VALUE ? Double.MAX_VALUE
                    : ticksToMs(segmentTicks, tp.mpq, resolution);

            if (segmentMs >= remainingMs) {
                long add = Math.round(remainingMs * resolution * 1000.0 / tp.mpq);
                ticksAdded += Math.max(0L, add);
                return ticksAdded;
            }

            if (segmentTicks != Long.MAX_VALUE) {
                ticksAdded += segmentTicks;
                remainingMs -= segmentMs;
                curTick = nextTick;
                if (idx + 1 < tempo.size()) {
                    idx++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        if (remainingMs > 0.0) {
            TempoPoint tp = tempo.get(idx);
            long add = Math.round(remainingMs * resolution * 1000.0 / tp.mpq);
            ticksAdded += Math.max(0L, add);
        }

        return ticksAdded;
    }

    private static List<TempoPoint> buildTempoMap(Sequence sequence) {
        List<TempoPoint> points = new ArrayList<>();
        points.add(new TempoPoint(0L, 500_000));

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent ev = track.get(i);
                MidiMessage msg = ev.getMessage();
                if (msg instanceof MetaMessage meta && meta.getType() == 0x51) {
                    byte[] data = meta.getData();
                    if (data != null && data.length >= 3) {
                        int mpq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        points.add(new TempoPoint(ev.getTick(), mpq));
                    }
                }
            }
        }

        points.sort(Comparator.comparingLong(tp -> tp.tick));

        Map<Long, TempoPoint> deduped = new LinkedHashMap<>();
        for (TempoPoint tp : points) {
            deduped.put(tp.tick, tp);
        }

        return new ArrayList<>(deduped.values());
    }

    private static int findTempoIndexAtOrBefore(List<TempoPoint> tempo, long tick) {
        int lo = 0;
        int hi = tempo.size() - 1;
        int ans = 0;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            long t = tempo.get(mid).tick;
            if (t <= tick) {
                ans = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return ans;
    }

    private static double ticksToMs(long ticks, int mpq, int resolution) {
        double us = (ticks * (double) mpq) / (double) resolution;
        return us / 1000.0;
    }

    private record TempoPoint(long tick, int mpq) {}
}

