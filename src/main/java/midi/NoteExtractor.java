package midi;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Objects;

/***
 * This class is responsible for extracting midi note information including on/off messages and extracting duration
 * each note is to be played. It will also hold information useful for transposing notes by figuring the midi track's
 * note range.
 */

public class NoteExtractor {

    private Sequence sequence;
    private int currentTrackIndex = 0;
    private ArrayList<SimpleNote> simpleOnNotes;
    private ArrayList<SimpleNote> simpleOffNotes;

    private static String noteOnByte = "9-";
    private static String noteOffByte = "8-";

    private byte lowestNote = 127;
    private byte highestNote = 0;

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
        for(Track track : sequence.getTracks()) {
            extractMessages(track);
            if(simpleOnNotes.size() > 0) {
                break;
            }
            currentTrackIndex++;
        }
    }

    public int extractMessages(Track oldTrack) {
        Track newTrack = sequence.createTrack();

        int extractedCount = 0;
        for (int msgIndex = 0; msgIndex < oldTrack.size(); msgIndex++) {

            MidiMessage midiMessage = oldTrack.get(msgIndex).getMessage();
            String status = MidiUtils.getSecondByte(midiMessage);
            long tick = oldTrack.get(msgIndex).getTick();

            if(status.equalsIgnoreCase(noteOnByte)) {

                updateNoteRange(midiMessage);
                int velocity = midiMessage.getMessage()[2];
                if(velocity != 0) {
                    if(isPolyphonicNote(simpleOnNotes, midiMessage, true, tick, msgIndex)) {
                        replaceNote(simpleOnNotes, new SimpleNote(midiMessage, true, midiMessage.getMessage()[1], tick, msgIndex));
                    }
                    else {
                        processOnNote(midiMessage, velocity, tick, msgIndex);
                        newTrack.add(oldTrack.get(msgIndex));
                    }
                    extractedCount++;
                }
                else {
                    if(isPolyphonicNote(simpleOffNotes, midiMessage, false, tick, msgIndex)) {
                        replaceNote(simpleOffNotes, new SimpleNote(midiMessage, false, midiMessage.getMessage()[1], tick, msgIndex));
                    }
                    else {
                        processOffNote(midiMessage, velocity, tick, msgIndex);
                        newTrack.add(oldTrack.get(msgIndex));
                    }
                    extractedCount++;
                }
            }

            else if(status.equalsIgnoreCase(noteOffByte)) {

                updateNoteRange(midiMessage);
                int velocity = midiMessage.getMessage()[2];
                if(isPolyphonicNote(simpleOffNotes, midiMessage, false, tick, msgIndex)) {
                    replaceNote(simpleOffNotes, new SimpleNote(midiMessage, false, midiMessage.getMessage()[1], tick, msgIndex));
                }
                else {
                    processOffNote(midiMessage, velocity, tick, msgIndex);
                    newTrack.add(oldTrack.get(msgIndex));
                }
                extractedCount++;
            }
            else {
                newTrack.add(oldTrack.get(msgIndex));
            }
        }
        if(extractedCount == 0) {
            sequence.deleteTrack(newTrack);
        }
        else {
            sequence.deleteTrack(oldTrack);
        }
        return extractedCount;
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
            ArrayList<MidiNote> onNotes, ArrayList<MidiNote> offNotes, double tickInMs) {

        ArrayList<Double> lengths = new ArrayList<>();
        lengths.add(onNotes.get(0).getTick() * tickInMs);
        MidiNote previousNote = onNotes.get(0);

        for(int i = 1; i < onNotes.size(); i++) {
            double duration = (onNotes.get(i).getTick() * tickInMs) - (previousNote.getTick() * tickInMs);
            lengths.add(duration);
            previousNote = onNotes.get(i);
        }
        lengths.add(offNotes.get(offNotes.size() - 1).getTick() * tickInMs -
                onNotes.get(onNotes.size() - 1).getTick() * tickInMs);

        return lengths;
    }

    public boolean isPolyphonicNote(ArrayList<SimpleNote> notes, MidiMessage midiMessage, boolean isOnNote, long tick, int msgIndex) {
        SimpleNote simpleNote = new SimpleNote(midiMessage, isOnNote, midiMessage.getMessage()[1], tick, msgIndex);
        boolean isPolyphony = notes.contains(simpleNote);
        if(isPolyphony) {
            return true;
        }
        else {
            return false;
        }
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

    public void updateNoteRange(MidiMessage midiMessage) {
        if(midiMessage.getMessage()[1] < lowestNote) {
            lowestNote = midiMessage.getMessage()[1];
        }
        if(midiMessage.getMessage()[1] > highestNote) {
            highestNote = midiMessage.getMessage()[1];
        }
    }

    public void transposeSequence() {
        this.sequence = Transposer.fitToRange(
                sequence, currentTrackIndex, lowestNote, highestNote, Ocarinas.C_SOPRANO);
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

    private class SimpleNote {
        private MidiMessage midiMessage;
        private String noteName;
        private boolean isOnNote;
        private byte noteValue;
        private long tick;
        private int msgIndex; // Used for tracking where the message appeared in the sequence track.

        public SimpleNote(MidiMessage midiMessage, boolean isOnNote, byte noteValue, long tick, int msgIndex) {
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

        public byte getNoteValue() {
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
