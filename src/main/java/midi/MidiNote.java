package midi;

import javax.sound.midi.MidiMessage;

public class MidiNote {

    private MidiMessage midiMessage;
    private String noteName;
    private boolean noteOn;
    private String statusHalfByte;
    private int velocity;
    private long tick;

    public MidiNote(MidiMessage midiMessage, String noteName, boolean noteOn, String statusHalfByte,
                    int velocity, long tick) {
        this.midiMessage = midiMessage;
        this.noteName = noteName;
        this.noteOn = noteOn;
        this.statusHalfByte = statusHalfByte;
        this.velocity = velocity;
        this.tick = tick;
    }

    public MidiMessage getMidiMessage() {
        return midiMessage;
    }

    public String getNoteName() {
        return noteName;
    }

    public boolean isNoteOn() {
        return noteOn;
    }

    public String getStatusHalfByte() {
        return statusHalfByte;
    }

    public int getVelocity() {
        return velocity;
    }

    public long getTick() {
        return tick;
    }
}
