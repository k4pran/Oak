package midi;

import javax.sound.midi.MidiMessage;
import java.util.Objects;

public class MidiNote {

    private MidiMessage midiMessage;
    private String noteName;
    private int noteValue;
    private boolean isOnMsg;
    private int velocity;
    private long tick;

    public MidiNote(MidiMessage midiMessage, String noteName, int noteValue, boolean isOnMsg,
                    int velocity, long tick) {
        this.midiMessage = midiMessage;
        this.noteName = noteName;
        this.noteValue = noteValue;
        this.isOnMsg = isOnMsg;
        this.velocity = velocity;
        this.tick = tick;
    }

    public MidiMessage getMidiMessage() {
        return midiMessage;
    }

    public String getNoteName() {
        return noteName;
    }

    public int getNoteValue() {
        return noteValue;
    }

    public boolean isOnMsg() {
        return isOnMsg;
    }

    public int getVelocity() {
        return velocity;
    }

    public long getTick() {
        return tick;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tick);
    }

    @Override
    public boolean equals(Object obj) {
        MidiNote midiNote = (MidiNote)obj;
        return this.tick == midiNote.tick;
    }


}
