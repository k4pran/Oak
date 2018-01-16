package midi;

import midi.MidiFile;
import validation.Validator;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Various methods for extracting information from midi notes.
 */

public class MidiNotes {

    //================================================================================
    // Static methods
    //================================================================================

    public static ArrayList<Integer> extractNotes(MidiFile midiFile) {
        Sequence sequence = midiFile.getSequence();
        String noteOnSig = "9-";
        ArrayList<Integer> notes = new ArrayList<>();
        ArrayList<Track> tracks = new ArrayList<>(Arrays.asList(sequence.getTracks()));

        System.out.println("Extracting notes...");

        for (int i = 0; i < tracks.size(); i++) {
            for (int j = 0; j < tracks.get(i).size(); j++) {
                MidiMessage midiMessage = tracks.get(i).get(j).getMessage();
                String status = getHalfStatusByte(midiMessage);
                if(status.equalsIgnoreCase(noteOnSig)) {
                    if(!isOffNote(midiMessage))
                    notes.add(Integer.parseInt(DatatypeConverter.printByte(midiMessage.getMessage()[1])));
                }
            }
        }
        if(notes.size() == 0) {
            throw new NoteProcessingException("Unable to extract notes from midi file");
        }

        System.out.println(notes.size() + " notes extracted successfully.\n");
        return notes;
    }

    public static ArrayList<Double> extractNoteLengths(MidiFile midiFile) {
        Sequence sequence = midiFile.getSequence();

        for(int trackIndex = 0; trackIndex < sequence.getTracks().length; trackIndex++) {
            NoteOnOffPairing pairing = new NoteOnOffPairing(sequence.getTracks()[trackIndex]);
            if(pairing.getOnNotes().size() > 0) {
                pairing.pairNotes();
                if(Validator.isPolyphonic(pairing.getOnNotes())) {
                    throw new PolyphonicException("Polyphonic midi files are not allowed. Ensure midi file only" +
                            "plays one note at a time.");
                }
                else {
                    return pairing.extractLengths(midiFile.getTicksInMs());
                }
            }
        }
        throw new NoteProcessingException("Unable to extract note lengths");
    }

    public static ArrayList<BufferedImage> mapNotesToImages(ArrayList<Integer> notes) {
        ArrayList<BufferedImage> ocarinaSprites = new ArrayList<>();

        System.out.println("Mapping notes to images...");
        for(Integer note : notes) {
            String mappedNote = formatNoteName(note);
            File ocarinaSprite = MapImage.altoCTwelve(mappedNote);
            try {
                ocarinaSprites.add(ImageIO.read(ocarinaSprite));
            }
            catch (IOException e) {
                throw new NoteProcessingException("Unable to convert ocarina sprite to image when mapping notes.");
            }
        }

        if(ocarinaSprites.size() == 0) {
            throw new NoteProcessingException("Unable to map notes to ocarina sprites");
        }

        System.out.println("All notes mapped successfully. " + ocarinaSprites.size() + " notes mapped.\n");
        return ocarinaSprites;
    }

    public static String formatNoteName(int x) {
        // Populate notes
        ArrayList<String> notes = new ArrayList<>(Arrays.asList(
                "C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"));
        String note = notes.get(x % (notes.size()));
        String octave = Integer.toString((x / 12));

        return note.concat(octave);
    }

    public static String getHalfStatusByte(MidiMessage midiMessage) {
        String statusByte = String.format("%2s", Integer.toHexString(midiMessage.getStatus())).replace(' ', '0');
        Character c = statusByte.charAt(0);
        StringBuilder sb = new StringBuilder();
        sb.append(c);
        sb.append('-');
        return sb.toString().toUpperCase();
    }

    public static boolean isOffNote(MidiMessage midiMessage) {
        return Integer.parseInt(DatatypeConverter.printByte(midiMessage.getMessage()[2])) == 0;
    }
}
