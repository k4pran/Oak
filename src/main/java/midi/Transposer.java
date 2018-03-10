package midi;

import javax.sound.midi.*;
import java.util.ArrayList;

public class Transposer {

    public static int transposedStep = 0;
    private static String noteOnByte = "9-";
    private static String noteOffByte = "8-";

    /**
     * Validates a midi sequence's range and transposes to fit if necessary.
     * It will alter the range based on the type of ocarina and the preferred key. E.g. C is preferred for a C soprano
     * ocarina. It will take three preferred keys in order and try to fit them, if this fails, it will align the
     * lowest note with the lowest on the ocarinas range. If it still doesn't fit it will throw an exception.
     * @param sequence
     * @param ocarina
     * @return
     */
    public static Sequence fitToRange(Sequence sequence, int trackNum, int lowestNote, int highestNote, Ocarinas ocarina) {
        int lowerOcRange = OcarinaRanges.getLowerRange(ocarina);
        int higherOcRange = OcarinaRanges.getUpperRange(ocarina);

        ArrayList<Integer> preferredKeys = Ocarinas.getPreferredKeys(ocarina);

        if(lowestNote < lowerOcRange && highestNote > higherOcRange) {
            throw new OutOfRangeException("Unable to transpose notes to fit ocarina range: " +
                    "\tOcarina: " + ocarina +
                    "\tLowest note in sequence: " + lowestNote +
                    "\tHighest note in sequence: " + highestNote +
                    "\tOcarina range: " + lowerOcRange + " - " + higherOcRange);
        }

        else if(lowestNote < lowerOcRange || highestNote > higherOcRange) {
            int stepUp = lowerOcRange - lowestNote;
            transposedStep = stepUp;

            Track track = sequence.createTrack();
            for(int i = 0; i < sequence.getTracks()[trackNum].size(); i++) {
                MidiMessage midiMessage = sequence.getTracks()[trackNum].get(i).getMessage();
                String status = MidiUtils.getSecondByte(midiMessage);

                if(status.equalsIgnoreCase(noteOnByte) || status.equalsIgnoreCase(noteOffByte)) {
                    try {
                        ShortMessage sm = transposeNote(sequence.getTracks()[trackNum].get(i), stepUp);
                        MidiEvent midiEvent = new MidiEvent(sm, sequence.getTracks()[trackNum].get(i).getTick());
                        track.add(midiEvent);
                    }
                    catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    track.add(sequence.getTracks()[0].get(i));
                }
            }
            sequence.deleteTrack(sequence.getTracks()[trackNum]);
        }
        return sequence;
    }

    private static ShortMessage transposeNote(MidiEvent midiEvent, int step) throws InvalidMidiDataException {
        byte[] data = midiEvent.getMessage().getMessage();
        data[1] += step;
        ShortMessage sm = new ShortMessage();
        byte channel = (byte)Character.digit(MidiUtils.toHexByte(midiEvent.getMessage().getMessage()[0]).charAt(1), 16);
        sm.setMessage(ShortMessage.NOTE_ON, channel, data[1], data[2]);
        return sm;
    }
}
