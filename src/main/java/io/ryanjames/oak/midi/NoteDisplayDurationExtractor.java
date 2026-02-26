package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.util.*;

/**
 * Computes "display durations" for notes: time from NOTE_ON(i) to NOTE_ON(i+1),
 * respecting tempo changes (Meta 0x51).
 *
 * This is ideal for video frames where the image stays until the next note starts.
 */
public final class NoteDisplayDurationExtractor {

    private final Integer channel; // null => auto-guess

    public NoteDisplayDurationExtractor(Integer channel) {
        if (channel != null && (channel < 0 || channel > 15)) {
            throw new IllegalArgumentException("channel must be 0..15 or null");
        }
        this.channel = channel;
    }

    public List<Double> extract(Sequence input) {
        Objects.requireNonNull(input, "input");
        if (input.getDivisionType() != Sequence.PPQ) {
            throw new IllegalArgumentException("Only PPQ sequences are supported");
        }

        int ch = (channel != null) ? channel : guessChannel(input);
        if (ch < 0) return List.of();

        return extractInterOnsetDurationsMs(input, ch);
    }

    private static int guessChannel(Sequence seq) {
        for (Track t : seq.getTracks()) {
            for (int i = 0; i < t.size(); i++) {
                MidiMessage msg = t.get(i).getMessage();
                if (msg instanceof ShortMessage sm) {
                    if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                        return sm.getChannel();
                    }
                }
            }
        }
        return -1;
    }

    private static List<Double> extractInterOnsetDurationsMs(Sequence seq, int channel) {
        // Merge all events across tracks and sort by tick (stable sort is useful)
        List<MidiEvent> events = new ArrayList<>();
        for (Track t : seq.getTracks()) {
            for (int i = 0; i < t.size(); i++) events.add(t.get(i));
        }
        events.sort(Comparator.comparingLong(MidiEvent::getTick));

        int mpq = 500_000; // default 120 BPM
        int res = seq.getResolution();

        long lastTick = 0;
        long curUs = 0;

        // collect NOTE_ON start times (absolute microseconds)
        List<Long> noteOnTimesUs = new ArrayList<>();

        for (MidiEvent ev : events) {
            long tick = ev.getTick();
            curUs += (tick - lastTick) * (long) mpq / res;
            lastTick = tick;

            MidiMessage mm = ev.getMessage();

            // Tempo change applies from this tick onwards
            if (mm instanceof MetaMessage meta && meta.getType() == 0x51) {
                byte[] data = meta.getData();
                if (data.length >= 3) {
                    mpq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                }
                continue;
            }

            if (!(mm instanceof ShortMessage sm)) continue;
            if (sm.getChannel() != channel) continue;

            boolean isOn = (sm.getCommand() == ShortMessage.NOTE_ON) && sm.getData2() > 0;
            if (isOn) {
                noteOnTimesUs.add(curUs);
            }
        }

        if (noteOnTimesUs.size() < 2) return List.of();

        List<Double> outMs = new ArrayList<>(noteOnTimesUs.size() - 1);
        for (int i = 0; i < noteOnTimesUs.size() - 1; i++) {
            long dtUs = noteOnTimesUs.get(i + 1) - noteOnTimesUs.get(i);
            if (dtUs < 0) dtUs = 0;
            outMs.add(dtUs / 1_000.0);
        }
        return outMs;
    }
}