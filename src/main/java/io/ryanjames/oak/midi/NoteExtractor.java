package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/***
 * This class is responsible for extracting midi note information including on/off messages and extracting duration
 * each note is to be played. It will also hold information useful for transposing notes by figuring the midi track's
 * note range.
 */

public class NoteExtractor {

    private Sequence sequence;
    private Integer melodyChannel = null;
    private int currentTrackIndex = 0;
    private ArrayList<SimpleNote> simpleOnNotes;
    private ArrayList<SimpleNote> simpleOffNotes;

    private static String noteOnByte = "9-";
    private static String noteOffByte = "8-";

    private int lowestNote = 127;
    private int highestNote = 0;

    public NoteExtractor(Sequence sequence) {
        this.sequence = sequence;
        this.simpleOnNotes = new ArrayList<>();
        this.simpleOffNotes = new ArrayList<>();
    }

    /**
     * Simple entry method for the extractor for readability.
     * Extracts on / off notes from sequence, removes lower polyphonic notes in sequence.
     */
    public void renderSequence() {
        extractTracks();
    }


    /**
     * Runs through midi messages in each track. This program can only process midi notes from one track and
     * so will break after the first track that contains note on messages.
     */
    public void extractTracks() {
        for (Track track : sequence.getTracks()) {

            // Find the first track that has ANY note-on across ANY channel
            if (trackHasAnyNotes(track)) {
                this.melodyChannel = guessMelodyChannel(track);
                extractMessages(track, this.melodyChannel);
                break;
            }

            currentTrackIndex++;
        }
    }

    private boolean trackHasAnyNotes(Track track) {
        for (int i = 0; i < track.size(); i++) {
            MidiMessage msg = track.get(i).getMessage();
            if (msg instanceof ShortMessage sm) {
                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) return true;
            }
        }
        return false;
    }

    public int extractMessages(Track oldTrack, int channelToKeep) {
        Track newTrack = sequence.createTrack();

        // tick -> best NOTE_ON event (highest note)
        java.util.Map<Long, MidiEvent> bestOnByTick = new java.util.HashMap<>();
        // tick -> best NOTE_OFF event (highest note) (covers NOTE_OFF and NOTE_ON vel=0)
        java.util.Map<Long, MidiEvent> bestOffByTick = new java.util.HashMap<>();

        int extractedCount = 0;

        // Pass 1: choose best note events per tick on the kept channel
        for (int i = 0; i < oldTrack.size(); i++) {
            MidiEvent evt = oldTrack.get(i);
            MidiMessage msg = evt.getMessage();

            if (!(msg instanceof ShortMessage sm)) continue;

            int cmd = sm.getCommand();
            int ch = sm.getChannel();
            if (ch != channelToKeep) continue;

            boolean isNoteOn = (cmd == ShortMessage.NOTE_ON && sm.getData2() > 0);
            boolean isNoteOff = (cmd == ShortMessage.NOTE_OFF) || (cmd == ShortMessage.NOTE_ON && sm.getData2() == 0);

            if (!isNoteOn && !isNoteOff) continue;

            long tick = evt.getTick();
            int note = sm.getData1();

            // update range based on ALL candidate notes (so transpose sees the real kept notes)
            updateNoteRange(note);

            if (isNoteOn) {
                MidiEvent existing = bestOnByTick.get(tick);
                if (existing == null || note > ((ShortMessage) existing.getMessage()).getData1()) {
                    bestOnByTick.put(tick, evt);
                }
                extractedCount++;
            } else {
                MidiEvent existing = bestOffByTick.get(tick);
                if (existing == null || note > ((ShortMessage) existing.getMessage()).getData1()) {
                    bestOffByTick.put(tick, evt);
                }
                extractedCount++;
            }
        }

        // Pass 2: rebuild track
        for (int i = 0; i < oldTrack.size(); i++) {
            MidiEvent evt = oldTrack.get(i);
            MidiMessage msg = evt.getMessage();
            long tick = evt.getTick();

            if (msg instanceof ShortMessage sm) {
                int cmd = sm.getCommand();
                int ch = sm.getChannel();

                boolean isNoteOn = (cmd == ShortMessage.NOTE_ON && sm.getData2() > 0);
                boolean isNoteOff = (cmd == ShortMessage.NOTE_OFF) || (cmd == ShortMessage.NOTE_ON && sm.getData2() == 0);

                if (isNoteOn) {
                    if (ch == channelToKeep && bestOnByTick.get(tick) == evt) {
                        // keep only the chosen note-on at this tick
                        newTrack.add(evt);
                        processOnNote(evt.getMessage(), sm.getData2(), tick, i);
                    }
                    continue; // drop other note-ons
                }

                if (isNoteOff) {
                    if (ch == channelToKeep && bestOffByTick.get(tick) == evt) {
                        // keep only the chosen note-off at this tick
                        newTrack.add(evt);
                        processOffNote(evt.getMessage(), sm.getData2(), tick, i);
                    }
                    continue; // drop other note-offs
                }

                // Non-note ShortMessage:
                // Option A: keep only from kept channel
                if (ch == channelToKeep) {
                    newTrack.add(evt);
                }
                // Option B (if you prefer): keep all non-note messages regardless of channel
                // newTrack.add(evt);

            } else {
                // MetaMessage / Sysex: keep (tempo, time signature, etc.)
                newTrack.add(evt);
            }
        }

        if (extractedCount == 0) {
            sequence.deleteTrack(newTrack);
        } else {
            sequence.deleteTrack(oldTrack);
        }

        return extractedCount;
    }


    public static void saveSequence(Sequence sequence, File outputFile)
            throws IOException {

        // 1 = standard MIDI file type 1 (multi-track)
        int[] types = MidiSystem.getMidiFileTypes(sequence);

        if (types.length == 0) {
            throw new IllegalArgumentException("Sequence cannot be written as a MIDI file");
        }

        // Usually type 1 is preferred if available
        int type = 1;
        if (!java.util.Arrays.stream(types).anyMatch(t -> t == 1)) {
            type = types[0]; // fallback
        }

        MidiSystem.write(sequence, type, outputFile);
    }

    public void processOnNote(MidiMessage onMessage, int velocity, long tick, int msgIndex) {
        SimpleNote simpleNote = new SimpleNote(onMessage, true, onMessage.getMessage()[1], tick, msgIndex);
        simpleOnNotes.add(simpleNote);
    }

    public void processOffNote(MidiMessage offMessage, int velocity, long tick, int msgIndex) {
        SimpleNote simpleNote = new SimpleNote(offMessage, false, offMessage.getMessage()[1], tick, msgIndex);
        simpleOffNotes.add(simpleNote);
    }

    public static ArrayList<Double> extractDurations(
            ArrayList<SimpleNote> onNotes, ArrayList<SimpleNote> offNotes, double tickInMs) {

        if (onNotes.isEmpty() || offNotes.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Double> lengths = new ArrayList<>();

        // Duration per note = time until the next NOTE_ON
        for (int i = 0; i < onNotes.size() - 1; i++) {
            long dtTicks = onNotes.get(i + 1).getTick() - onNotes.get(i).getTick();
            double durationMs = dtTicks * tickInMs;

            // guard: never return 0ms (prevents "missing first note" in frame renderers)
            if (durationMs <= 0) durationMs = tickInMs;

            lengths.add(durationMs);
        }

        // Last note duration = lastOff - lastOn
        long lastOnTick = onNotes.get(onNotes.size() - 1).getTick();
        long lastOffTick = offNotes.get(offNotes.size() - 1).getTick();
        double lastMs = (lastOffTick - lastOnTick) * tickInMs;

        if (lastMs <= 0) lastMs = tickInMs;
        lengths.add(lastMs);

        return lengths;
    }

    private boolean isPolyphonicTick(ArrayList<SimpleNote> notes, long tick) {
        for (SimpleNote n : notes) {
            if (n.getTick() == tick) return true;
        }
        return false;
    }

    private void replaceNoteAtTick(ArrayList<SimpleNote> notes, SimpleNote newNote) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getTick() == newNote.getTick()) {
                SimpleNote old = notes.get(i);
                if (newNote.getNoteValue() > old.getNoteValue()) {
                    notes.set(i, newNote);
                }
                return;
            }
        }
        notes.add(newNote);
    }

    /**
     * If the new note is of a higher note value it will replace the old in the ArrayList and track.
     * If the new note is of a lower note value it will note add to the ArrayList, but delete it from the track.
     * @param newNote is the potential replacement note.
     * @return true if note is replaced
     */
    public boolean replaceNote(ArrayList<SimpleNote> notes, SimpleNote newNote) {
        int oldIndex = notes.indexOf(newNote);
        SimpleNote oldNote = notes.get(oldIndex);
        if (newNote.getNoteValue() > oldNote.getNoteValue()) {
            notes.set(oldIndex, newNote);
            return true;
        }
        else {
            notes.add(newNote);
            return false;
        }
    }

    public void updateNoteRange(int note) {
        if (note < lowestNote) lowestNote = note;
        if (note > highestNote) highestNote = note;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public ArrayList<SimpleNote> getSimpleOnNotes() {
        return simpleOnNotes;
    }

    public ArrayList<SimpleNote> getSimpleOffNotes() {
        return simpleOffNotes;
    }

    public ArrayList<MidiNote> simpleToMidiNotes(ArrayList<SimpleNote> simpleNotes) {
        ArrayList<MidiNote> midiNotes = new ArrayList<>();
        for(SimpleNote simpleNote : simpleNotes) {
            midiNotes.add(new MidiNote(
                    simpleNote.getMidiMessage(),
                    MidiUtils.formatNoteName(simpleNote.noteValue + Transposer.transposedStep),
                    simpleNote.getNoteValue() + Transposer.transposedStep,
                    simpleNote.isOnNote(),
                    simpleNote.getMidiMessage().getMessage()[2],
                    simpleNote.getTick()
            ));
        }
        return midiNotes;
    }

    private int guessMelodyChannel(Track track) {
        // 1) Quick win: if channel 0 has notes, use it
        int channel0Notes = countNoteOns(track, 0);
        if (channel0Notes > 0) return 0;

        // 2) Otherwise: "top note per tick" heuristic
        // Map tick -> (channel -> highestNoteAtThatTickForThatChannel)
        // Then at each tick pick the channel with the highest note, tally winners.

        java.util.Map<Long, java.util.Map<Integer, Integer>> tickToChannelTop = new java.util.HashMap<>();

        for (int i = 0; i < track.size(); i++) {
            MidiMessage msg = track.get(i).getMessage();
            if (!(msg instanceof ShortMessage sm)) continue;

            int cmd = sm.getCommand();
            if (cmd != ShortMessage.NOTE_ON) continue;

            int vel = sm.getData2();
            if (vel == 0) continue; // treat as off

            long tick = track.get(i).getTick();
            int ch = sm.getChannel();
            int note = sm.getData1();

            tickToChannelTop
                    .computeIfAbsent(tick, t -> new java.util.HashMap<>())
                    .merge(ch, note, Math::max);
        }

        int[] wins = new int[16];

        for (var entry : tickToChannelTop.entrySet()) {
            var channelTop = entry.getValue();
            int bestCh = -1;
            int bestNote = -1;
            for (var e : channelTop.entrySet()) {
                if (e.getValue() > bestNote) {
                    bestNote = e.getValue();
                    bestCh = e.getKey();
                }
            }
            if (bestCh >= 0) wins[bestCh]++;
        }

        int bestCh = 0;
        int bestWins = wins[0];
        for (int ch = 1; ch < 16; ch++) {
            if (wins[ch] > bestWins) {
                bestWins = wins[ch];
                bestCh = ch;
            }
        }

        // If everything is empty, default to 0
        return bestWins == 0 ? 0 : bestCh;
    }

    private int countNoteOns(Track track, int channel) {
        int count = 0;
        for (int i = 0; i < track.size(); i++) {
            MidiMessage msg = track.get(i).getMessage();
            if (!(msg instanceof ShortMessage sm)) continue;
            if (sm.getChannel() != channel) continue;
            if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) count++;
        }
        return count;
    }

    private class SimpleNote {
        private MidiMessage midiMessage;
        private String noteName;
        private boolean isOnNote;
        private int noteValue;
        private long tick;
        private int msgIndex; // Used for tracking where the message appeared in the sequence track.

        public SimpleNote(MidiMessage midiMessage, boolean isOnNote, int noteValue, long tick, int msgIndex) {
            this.midiMessage = midiMessage;
            this.isOnNote = isOnNote;
            this.noteName = MidiUtils.getNoteName(midiMessage);
            this.noteValue = noteValue;
            this.tick = tick;
            this.msgIndex = msgIndex;
        }

        public MidiMessage getMidiMessage() {
            return midiMessage;
        }

        public String getNoteName() {
            return noteName;
        }

        public boolean isOnNote() {
            return isOnNote;
        }

        public int getNoteValue() {
            return noteValue;
        }

        public long getTick() {
            return tick;
        }

        public int getMsgIndex() {
            return msgIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tick);
        }

        @Override
        public boolean equals(Object obj) {
            SimpleNote simpleNote = (SimpleNote) obj;
            return this.tick == simpleNote.tick;
        }
    }
}
