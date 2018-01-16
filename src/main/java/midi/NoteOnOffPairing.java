package midi;

import midi.MidiNote;
import midi.MidiNotes;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;
import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;

public class NoteOnOffPairing {
    private ArrayList<NotePair> notePairs;
    private ArrayList<MidiNote> unpaired;
    private ArrayList<MidiNote> onNotes;
    private ArrayList<MidiNote> offNotes;

    private static String noteOnByte = "9-";
    private static String noteOffByte = "8-";

    public NoteOnOffPairing(Track track) {
        this.notePairs = new ArrayList<>();
        this.unpaired = new ArrayList<>();
        this.onNotes = new ArrayList<>();
        this.offNotes = new ArrayList<>();
        extractNotes(track);
    }

    public NoteOnOffPairing(ArrayList<MidiNote> onNotes, ArrayList<MidiNote> offNotes) {
        this.notePairs = new ArrayList<>();
        this.unpaired = new ArrayList<>();
        this.onNotes = onNotes;
        this.offNotes = offNotes;
    }

    public void pairNotes() {
        boolean matched = false;

        for(int i = 0; i < onNotes.size(); i++) {
            notePairs.add(new NotePair(onNotes.get(i), offNotes.get(i)));
        }
    }

    private void extractNotes(Track track) {
        for(int msgIndex = 0; msgIndex < track.size(); msgIndex++) {
            MidiMessage midiMessage = track.get(msgIndex).getMessage();
            String status = MidiNotes.getHalfStatusByte(midiMessage);
            if(status.equalsIgnoreCase(noteOnByte)) {
                String noteName = MidiNotes.formatNoteName(
                        Integer.parseInt(DatatypeConverter.printByte(midiMessage.getMessage()[1])));
                int velocity = midiMessage.getMessage()[2];

                if(velocity != 0) {
                    onNotes.add(new MidiNote(midiMessage, noteName, true, status, velocity, track.get(msgIndex).getTick()));
                }
                else {
                    offNotes.add(new MidiNote(midiMessage, noteName, false, status, velocity, track.get(msgIndex).getTick()));
                }
            }
            else if(status.equalsIgnoreCase(noteOffByte)) {
                String noteName = MidiNotes.formatNoteName(
                        Integer.parseInt(DatatypeConverter.printByte(midiMessage.getMessage()[1])));
                int velocity = midiMessage.getMessage()[2];

                offNotes.add(new MidiNote(midiMessage, noteName, false, status, velocity, track.get(msgIndex).getTick()));
            }
        }
    }

    /**
     * Finds length of each note using the paired on/off notes.
     * @param tickInMs
     * @return
     */
//    public ArrayList<Double> extractLengths(double tickInMs) {
//        ArrayList<Double> lengths = new ArrayList<>();
//
//        lengths.add(notePairs.get(0).getNoteOn().getTick() * tickInMs);
//
//        for(NotePair notePair : notePairs) {
//            lengths.add((notePair.noteOff.getTick() * tickInMs) - (notePair.noteOn.getTick() * tickInMs));
//        }
//        return lengths;
//    }

        public ArrayList<Double> extractLengths(double tickInMs) {
        ArrayList<Double> lengths = new ArrayList<>();

            lengths.add(onNotes.get(0).getTick() * tickInMs);

            MidiNote previousNote = onNotes.get(0);
            for(int i = 1; i < onNotes.size(); i++) {
            lengths.add((onNotes.get(i).getTick() * tickInMs) - (previousNote.getTick() * tickInMs));
            previousNote = onNotes.get(i);
        }
        lengths.add(offNotes.get(offNotes.size() - 1).getTick() * tickInMs -
                onNotes.get(onNotes.size() - 1).getTick() * tickInMs);

        return lengths;
    }

    private boolean doNotesPair(MidiNote onNote, MidiNote offNote) {
        return onNote.getNoteName().equalsIgnoreCase(offNote.getNoteName());
    }

    public ArrayList<NotePair> getNotePairs() {
        return notePairs;
    }

    public ArrayList<MidiNote> getUnpaired() {
        return unpaired;
    }

    public ArrayList<MidiNote> getOnNotes() {
        return onNotes;
    }

    public ArrayList<MidiNote> getOffNotes() {
        return offNotes;
    }

    public class NotePair {
        private MidiNote noteOn;
        private MidiNote noteOff;

        public NotePair(MidiNote noteOn, MidiNote noteOff) {
            this.noteOn = noteOn;
            this.noteOff = noteOff;
        }

        public MidiNote getNoteOn() {
            return noteOn;
        }

        public MidiNote getNoteOff() {
            return noteOff;
        }
    }
}
