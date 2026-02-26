package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.util.Objects;

/**
 * Picks the track most likely to contain the melody by counting NOTE_ON (vel>0) events.
 * Tie-breakers:
 *  1) more distinct note-on ticks (less “drum-roll” density on same tick)
 *  2) wider pitch range
 *  3) lower track index
 *
 * Returns the Track from the input Sequence (not a copy).
 */
public final class MajorityMelodyTrackTransformer implements SequenceTransformer {

    private final Integer preferredChannel; // optional: restrict counting to a channel (0..15)

    public MajorityMelodyTrackTransformer() {
        this(null);
    }

    public MajorityMelodyTrackTransformer(Integer preferredChannel) {
        if (preferredChannel != null && (preferredChannel < 0 || preferredChannel > 15)) {
            throw new IllegalArgumentException("preferredChannel must be 0..15 or null");
        }
        this.preferredChannel = preferredChannel;
    }

    @Override
    public Sequence transform(Sequence input) throws InvalidMidiDataException {
        Objects.requireNonNull(input, "input");

        Track[] tracks = input.getTracks();
        if (tracks.length == 0) {
            return new Sequence(input.getDivisionType(), input.getResolution(), 1);
        }

        int bestIdx = -1;
        Stats best = null;

        for (int i = 0; i < tracks.length; i++) {
            Stats s = analyze(tracks[i], preferredChannel);
            if (best == null || s.betterThan(best)) {
                best = s;
                bestIdx = i;
            }
        }

        if (best.noteOns == 0) {
            throw new IllegalStateException("Unable to determine melody track (no NOTE_ON found)");
        }

        return MidiCopyUtils.singleTrackCopy(input, tracks[bestIdx]);
    }

    private static Stats analyze(Track track, Integer preferredChannel) {
        int noteOns = 0;
        int distinctOnTicks = 0;

        long lastOnTick = Long.MIN_VALUE;

        int minPitch = 127;
        int maxPitch = 0;
        boolean any = false;

        for (int i = 0; i < track.size(); i++) {
            MidiMessage mm = track.get(i).getMessage();
            if (!(mm instanceof ShortMessage sm)) continue;

            if (sm.getCommand() != ShortMessage.NOTE_ON) continue;
            if (sm.getData2() <= 0) continue; // vel 0 treated as off

            if (preferredChannel != null && sm.getChannel() != preferredChannel) continue;

            noteOns++;
            any = true;

            long tick = track.get(i).getTick();
            if (tick != lastOnTick) {
                distinctOnTicks++;
                lastOnTick = tick;
            }

            int pitch = sm.getData1();
            if (pitch < minPitch) minPitch = pitch;
            if (pitch > maxPitch) maxPitch = pitch;
        }

        int pitchRange = any ? (maxPitch - minPitch) : 0;
        return new Stats(noteOns, distinctOnTicks, pitchRange);
    }

    private static final class Stats {
        final int noteOns;
        final int distinctOnTicks;
        final int pitchRange;

        Stats(int noteOns, int distinctOnTicks, int pitchRange) {
            this.noteOns = noteOns;
            this.distinctOnTicks = distinctOnTicks;
            this.pitchRange = pitchRange;
        }

        boolean betterThan(Stats other) {
            if (this.noteOns != other.noteOns) return this.noteOns > other.noteOns;
            if (this.distinctOnTicks != other.distinctOnTicks) return this.distinctOnTicks > other.distinctOnTicks;
            return this.pitchRange > other.pitchRange;
        }
    }
}