package midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;


public class MidiRecorder {



    public  MidiRecorder() {
        try {
            Sequence sequence = MidiSystem.getSequence(new File("com/company/Jingle Bells.mid"));


            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(sequence);
        }
        catch (InvalidMidiDataException | IOException | MidiUnavailableException e) {

        }
    }

}
