package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.util.*;

/**
 * Skyline melody extraction (top voice):
 * At any time, choose the highest-pitch currently-sounding note.
 *
 * Output is a single-track, monophonic sequence containing:
 * - selected NOTE_ON/NOTE_OFF events
 * - copied MetaMessages (tempo/time-signature/etc.) from the original sequence
 */
public class SkylineMelodyTransformer implements SequenceTransformer {

    @Override
    public Sequence transform(Sequence input) {
        Objects.requireNonNull(input, "input sequence");

        final List<Note> notes = parseNotes(input);
        final List<MetaEvent> metas = collectMetaEvents(input);

        try {
            // New 1-track sequence (use the existing track; don't createTrack() again)
            Sequence out = new Sequence(input.getDivisionType(), input.getResolution(), 1);
            Track outTrack = out.getTracks()[0];

            // Copy meta messages (tempo, time signature, track name, etc.) using the copy util
            for (MetaEvent me : metas) {
                MidiMessage msgCopy = MidiCopyUtils.deepCopyMessage(me.message);
                outTrack.add(new MidiEvent(msgCopy, me.tick));
            }

            // Skyline-select note segments
            List<NoteSegment> segments = skylineSegments(notes);

            // Emit segments as NOTE_ON/NOTE_OFF pairs on channel 0
            for (NoteSegment seg : segments) {
                if (seg.endTick <= seg.startTick) continue;

                outTrack.add(new MidiEvent(MidiCopyUtils.deepCopyMessage(noteOn(0, seg.pitch, seg.velocity)), seg.startTick));
                outTrack.add(new MidiEvent(MidiCopyUtils.deepCopyMessage(noteOff(0, seg.pitch, 0)), seg.endTick));
            }

            // Ensure End-of-Track meta exists at end (use the util)
            MidiCopyUtils.ensureEndOfTrack(outTrack);

            return out;
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException("Failed to construct extracted MIDI sequence", e);
        }
    }

    /**
     * Represents a fully paired NOTE (start/end).
     */
    private static final class Note {
        final long startTick;
        final long endTick;
        final int pitch;     // 0..127
        final int velocity;  // 1..127 (for NOTE_ON)

        Note(long startTick, long endTick, int pitch, int velocity) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.pitch = pitch;
            this.velocity = velocity;
        }
    }

    /**
     * Represents chosen monophonic segment.
     */
    private static final class NoteSegment {
        final long startTick;
        final long endTick;
        final int pitch;
        final int velocity;

        NoteSegment(long startTick, long endTick, int pitch, int velocity) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.pitch = pitch;
            this.velocity = velocity;
        }
    }

    private static final class MetaEvent {
        final long tick;
        final MetaMessage message;

        MetaEvent(long tick, MetaMessage message) {
            this.tick = tick;
            this.message = message;
        }
    }

    private static final class StartInfo {
        final long startTick;
        final int velocity;

        StartInfo(long startTick, int velocity) {
            this.startTick = startTick;
            this.velocity = velocity;
        }
    }

    /**
     * Parse NOTE_ON/NOTE_OFF into paired notes (across all tracks/channels).
     * NOTE_ON with velocity 0 is treated as NOTE_OFF.
     */
    private static List<Note> parseNotes(Sequence seq) {
        // Key: channel(0..15) + pitch(0..127)
        // Value: stack of NOTE_ON starts (handles overlapping same pitch on same channel)
        Deque<StartInfo>[] stacks = new ArrayDeque[16 * 128];
        for (int i = 0; i < stacks.length; i++) stacks[i] = new ArrayDeque<>();

        List<Note> out = new ArrayList<>();

        for (Track track : seq.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent ev = track.get(i);
                MidiMessage mm = ev.getMessage();
                if (!(mm instanceof ShortMessage sm)) continue;

                int cmd = sm.getCommand();
                int ch = sm.getChannel();
                int pitch = sm.getData1();
                int vel = sm.getData2();

                boolean isNoteOn = (cmd == ShortMessage.NOTE_ON) && vel > 0;
                boolean isNoteOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && vel == 0);

                if (!isNoteOn && !isNoteOff) continue;

                int key = ch * 128 + pitch;

                if (isNoteOn) {
                    stacks[key].push(new StartInfo(ev.getTick(), vel));
                } else {
                    StartInfo start = stacks[key].poll(); // match most recent start
                    if (start != null) {
                        long endTick = ev.getTick();
                        if (endTick > start.startTick) {
                            out.add(new Note(start.startTick, endTick, pitch, start.velocity));
                        }
                    }
                }
            }
        }

        // Sort notes by startTick, then pitch (ascending)
        out.sort(Comparator.<Note>comparingLong(n -> n.startTick)
                .thenComparingInt(n -> n.pitch)
                .thenComparingLong(n -> n.endTick));

        return out;
    }

    /**
     * Skyline selection by sweeping "note start/end" events:
     * maintain active notes; between event times choose highest pitch.
     */
    private static List<NoteSegment> skylineSegments(List<Note> notes) {
        if (notes.isEmpty()) return List.of();

        // Create boundary events: start/end
        record Boundary(long tick, boolean isStart, Note note) {}
        List<Boundary> boundaries = new ArrayList<>(notes.size() * 2);
        for (Note n : notes) {
            boundaries.add(new Boundary(n.startTick, true, n));
            boundaries.add(new Boundary(n.endTick, false, n));
        }

        // Sort boundaries by tick; process ends before starts at same tick
        boundaries.sort(Comparator.<Boundary>comparingLong(Boundary::tick)
                .thenComparing(b -> b.isStart ? 1 : 0));

        // Active notes keyed by pitch -> multiset of velocities (so we can pick max velocity for that pitch)
        TreeMap<Integer, PriorityQueue<Integer>> active = new TreeMap<>();
        List<NoteSegment> segments = new ArrayList<>();

        long prevTick = boundaries.get(0).tick();
        Integer currentPitch = null;
        Integer currentVel = null;

        int idx = 0;
        while (idx < boundaries.size()) {
            long tick = boundaries.get(idx).tick();

            // Emit segment for [prevTick, tick) using current selection
            if (currentPitch != null && tick > prevTick) {
                segments.add(new NoteSegment(prevTick, tick, currentPitch, currentVel != null ? currentVel : 64));
            }

            // Apply all boundaries at this tick
            while (idx < boundaries.size() && boundaries.get(idx).tick() == tick) {
                Boundary b = boundaries.get(idx);
                Note n = b.note();

                if (b.isStart()) {
                    active.computeIfAbsent(n.pitch, p -> new PriorityQueue<>(Comparator.reverseOrder()))
                            .add(n.velocity);
                } else {
                    PriorityQueue<Integer> pq = active.get(n.pitch);
                    if (pq != null) {
                        // remove one occurrence of this velocity if present; otherwise just pop one
                        if (!pq.remove(n.velocity) && !pq.isEmpty()) pq.poll();
                        if (pq.isEmpty()) active.remove(n.pitch);
                    }
                }
                idx++;
            }

            // Choose new skyline note: highest pitch among active
            if (active.isEmpty()) {
                currentPitch = null;
                currentVel = null;
            } else {
                Map.Entry<Integer, PriorityQueue<Integer>> top = active.lastEntry();
                currentPitch = top.getKey();
                currentVel = top.getValue().peek(); // max velocity for that pitch
            }

            prevTick = tick;
        }

        // No need to add trailing segment after last boundary (nothing active)
        // Merge adjacent segments with same pitch (just in case)
        return mergeAdjacent(segments);
    }

    private static List<NoteSegment> mergeAdjacent(List<NoteSegment> segs) {
        if (segs.isEmpty()) return segs;
        List<NoteSegment> merged = new ArrayList<>();
        NoteSegment cur = segs.get(0);

        for (int i = 1; i < segs.size(); i++) {
            NoteSegment nxt = segs.get(i);
            if (cur.pitch == nxt.pitch && cur.endTick == nxt.startTick) {
                // keep higher velocity (or keep current)
                int vel = Math.max(cur.velocity, nxt.velocity);
                cur = new NoteSegment(cur.startTick, nxt.endTick, cur.pitch, vel);
            } else {
                merged.add(cur);
                cur = nxt;
            }
        }
        merged.add(cur);
        return merged;
    }

    private static List<MetaEvent> collectMetaEvents(Sequence seq) {
        List<MetaEvent> metas = new ArrayList<>();
        for (Track track : seq.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent ev = track.get(i);
                MidiMessage mm = ev.getMessage();
                if (mm instanceof MetaMessage meta) {
                    // Skip End-of-Track here; we will add a fresh one at the end
                    if (meta.getType() == 0x2F) continue;
                    metas.add(new MetaEvent(ev.getTick(), meta));
                }
            }
        }
        metas.sort(Comparator.comparingLong(m -> m.tick));
        return metas;
    }

    private static long computeEndTick(Track t) {
        long max = 0;
        for (int i = 0; i < t.size(); i++) {
            max = Math.max(max, t.get(i).getTick());
        }
        return max;
    }

    private static ShortMessage noteOn(int channel, int pitch, int velocity) throws InvalidMidiDataException {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, channel, clamp7(pitch), clamp7(velocity));
        return sm;
    }

    private static ShortMessage noteOff(int channel, int pitch, int velocity) throws InvalidMidiDataException {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_OFF, channel, clamp7(pitch), clamp7(velocity));
        return sm;
    }

    private static MetaMessage endOfTrack() throws InvalidMidiDataException {
        MetaMessage mm = new MetaMessage();
        mm.setMessage(0x2F, new byte[0], 0);
        return mm;
    }

    private static int clamp7(int v) {
        return Math.max(0, Math.min(127, v));
    }
}
