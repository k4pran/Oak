package midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MidiCropper {
    private Sequence sequence;
    private static String noteOnByte = "9-";

    public MidiCropper(Sequence sequence) {
        this.sequence = sequence;
    }

    public Sequence cropDuplicates() {
        ArrayList<ArrayList<MidiEvent>> croppedEventArrays = new ArrayList<>();

        for(Track oldTrack : sequence.getTracks()) {

            ArrayList<Integer> noteIndices = MidiUtils.getNoteIndices(oldTrack, true, true);
            ArrayList<MidiEvent> midiEvents = new ArrayList<>();
            ArrayList<MiniNote> simpleNotes = new ArrayList<>(Collections.nCopies(oldTrack.size(), null));

            int simpleNotePos = 0;
            for(int i = 0; i < oldTrack.size(); i++) {
                if(noteIndices.contains(i)) {
                    MidiMessage msg = oldTrack.get(i).getMessage();
                    oldTrack.get(i).setTick(oldTrack.get(i).getTick() + 1);
                    long tick = oldTrack.get(i).getTick();
                    boolean isOnNote = MidiUtils.getSecondByte(msg).equalsIgnoreCase(noteOnByte) && msg.getMessage()[2] > 0;
                    MiniNote newNote = new MiniNote(msg, msg.getMessage()[1], isOnNote, tick + 1);

                    if(simpleNotes.contains(newNote)) {
                        int storedIndex = simpleNotes.indexOf(newNote);
                        MiniNote storedNote = simpleNotes.get(storedIndex);
                        if(newNote.getNoteValue() > storedNote.getNoteValue()) {
                            simpleNotes.set(storedIndex, newNote);
                            midiEvents.set(storedIndex, oldTrack.get(i));
                        }
                        else {
                            simpleNotePos++;
                        }
                    }
                    else {
                        simpleNotes.set(simpleNotePos, newNote);
                        midiEvents.add(oldTrack.get(i));
                        simpleNotePos++;
                    }
                }
                else {
                    midiEvents.add(oldTrack.get(i));
                    simpleNotePos++;
                }
            }
            croppedEventArrays.add(midiEvents);
        }
        purgeSequence();
        fillSequence(croppedEventArrays);
        return sequence;
    }

    private void purgeSequence() {
        for(Track track : sequence.getTracks()) {
            sequence.deleteTrack(track);
        }
    }

    private void fillSequence(ArrayList<ArrayList<MidiEvent>> midiEventArrays) {
        for(ArrayList<MidiEvent> midiEventArray : midiEventArrays) {
            Track track = sequence.createTrack();
            for(MidiEvent midiEvent : midiEventArray) {
                track.add(midiEvent);
            }
        }
    }


    private class MiniNote {
        private String noteName;
        private byte noteValue;
        private boolean isOnNote;
        private long tick;

        public MiniNote(MidiMessage midiMessage, byte noteValue, boolean isOnNote, long tick) {
            this.noteName = MidiUtils.getNoteName(midiMessage);
            this.noteValue = noteValue;
            this.isOnNote = isOnNote;
            this.tick = tick;
        }

        public byte getNoteValue() {
            return noteValue;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tick);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }
            MiniNote simpleNote = (MiniNote) obj;
            return (this.tick == simpleNote.tick) && (this.isOnNote == simpleNote.isOnNote);
        }
    }
}
