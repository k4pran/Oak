package io.ryanjames.oak.midi;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Tempo-aware version of the old "duration = time until next NOTE_ON" logic.
 *
 * - Uses tempo map (Meta 0x51) from the sequence
 * - Computes ms between tick ranges by integrating over tempo segments
 */
public final class TempoAwareDurations {

    private TempoAwareDurations() {}

    /**
     * Minimal contract needed from your SimpleNote.
     * If your SimpleNote already exists, you don't need this interface;
     * just keep the method signature and call getTick() on your type.
     */
    public interface HasTick {
        long getTick();
    }

    private record TempoPoint(long tick, int mpq) {} // microseconds per quarter note

    public static ArrayList<Double> extractDurations(
            Sequence seq,
            List<MidiNote> onNotes,
            List<MidiNote> offNotes
    ) {
        if (seq == null) throw new IllegalArgumentException("seq is null");
        if (seq.getDivisionType() != Sequence.PPQ) {
            throw new IllegalArgumentException("Only PPQ sequences are supported");
        }
        if (onNotes == null || offNotes == null || onNotes.isEmpty() || offNotes.isEmpty()) {
            return new ArrayList<>();
        }

        final int res = seq.getResolution();
        final List<TempoPoint> tempo = buildTempoMap(seq);

        ArrayList<Double> lengths = new ArrayList<>(onNotes.size());

        // Duration per note = time until next NOTE_ON (tempo-aware)
        for (int i = 0; i < onNotes.size() - 1; i++) {
            long a = onNotes.get(i).getTick();
            long b = onNotes.get(i + 1).getTick();

            double ms = microsBetweenTicks(a, b, res, tempo) / 1_000.0;

            // guard: never 0ms; use 1 tick at the current tempo at tick a
            if (ms <= 0) ms = oneTickMsAt(a, res, tempo);

            lengths.add(ms);
        }

        // Last note duration = lastOff - lastOn (tempo-aware)
        long lastOnTick = onNotes.get(onNotes.size() - 1).getTick();
        long lastOffTick = offNotes.get(offNotes.size() - 1).getTick();

        double lastMs = microsBetweenTicks(lastOnTick, lastOffTick, res, tempo) / 1_000.0;
        if (lastMs <= 0) lastMs = oneTickMsAt(lastOnTick, res, tempo);

        lengths.add(lastMs);

        return lengths;
    }

    private static List<TempoPoint> buildTempoMap(Sequence seq) {
        // Default tempo 120 BPM = 500,000 MPQ
        ArrayList<TempoPoint> points = new ArrayList<>();
        points.add(new TempoPoint(0, 500_000));

        for (Track t : seq.getTracks()) {
            for (int i = 0; i < t.size(); i++) {
                MidiEvent ev = t.get(i);
                MidiMessage mm = ev.getMessage();
                if (mm instanceof MetaMessage meta && meta.getType() == 0x51) {
                    byte[] data = meta.getData();
                    if (data.length >= 3) {
                        int mpq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        points.add(new TempoPoint(ev.getTick(), mpq));
                    }
                }
            }
        }

        points.sort(Comparator.comparingLong(TempoPoint::tick));

        // De-dupe: if multiple tempo points at same tick, keep the last one (common in messy files)
        ArrayList<TempoPoint> deduped = new ArrayList<>();
        for (TempoPoint p : points) {
            if (!deduped.isEmpty() && deduped.get(deduped.size() - 1).tick == p.tick) {
                deduped.set(deduped.size() - 1, p);
            } else {
                deduped.add(p);
            }
        }
        return deduped;
    }

    /**
     * Integrate microseconds between [startTick, endTick) over tempo segments.
     */
    private static long microsBetweenTicks(long startTick, long endTick, int res, List<TempoPoint> tempo) {
        if (endTick <= startTick) return 0;

        long us = 0;
        long cur = startTick;

        // Find the active tempo at startTick
        int idx = findTempoIndexAtOrBefore(tempo, startTick);

        while (cur < endTick) {
            TempoPoint tp = tempo.get(idx);
            long segStart = cur;
            long nextTempoTick = (idx + 1 < tempo.size()) ? tempo.get(idx + 1).tick : Long.MAX_VALUE;
            long segEnd = Math.min(endTick, nextTempoTick);

            long dtTicks = segEnd - segStart;
            // microseconds = ticks * (mpq / res)
            us += dtTicks * (long) tp.mpq / res;

            cur = segEnd;
            if (cur < endTick && idx + 1 < tempo.size() && tempo.get(idx + 1).tick == cur) {
                idx++;
            } else if (cur >= nextTempoTick && idx + 1 < tempo.size()) {
                idx++;
            }
        }

        return us;
    }

    private static int findTempoIndexAtOrBefore(List<TempoPoint> tempo, long tick) {
        // tempo list is sorted and non-empty, starts at tick 0.
        int lo = 0, hi = tempo.size() - 1, ans = 0;
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

    private static double oneTickMsAt(long tick, int res, List<TempoPoint> tempo) {
        int idx = findTempoIndexAtOrBefore(tempo, tick);
        int mpq = tempo.get(idx).mpq;
        // 1 tick = mpq/res microseconds
        return (mpq / (double) res) / 1_000.0;
    }
}