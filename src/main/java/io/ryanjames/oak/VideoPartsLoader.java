//import midi.*;
//import org.apache.commons.cli.CommandLine;
//import validation.Validator;
//
//import javax.sound.midi.Sequence;
//import java.io.File;
//import java.util.ArrayList;
//
//public class VideoPartsLoader {
//
//    private MidiFile midiFile;
//    private Sequence sequence;
//    private ArrayList<MidiNote> midiNotes;
//    private ArrayList<MidiNote> offNotes;
//    private File background;
//    private File audio;
//    private String outputFilePath;
//
//    private boolean audioFromMidi = false;
//
//    private final CommandLine cmd;
//
//    public VideoPartsLoader(CommandLine cmd) throws CommandLineException {
//        this.cmd = cmd;
//        load();
//    }
//
//    private void load() throws CommandLineException {
//        if(cmd.hasOption("i")) {
//            String midiPath = cmd.getOptionValue("i");
//            try {
//                midiFile = new MidiFile(new File(midiPath));
//                this.sequence = midiFile.getSequence();
//
//                MidiCropper midiCropper = new MidiCropper(sequence);
//                sequence = midiCropper.cropDuplicates();
//                NoteExtractor noteExtractor = new NoteExtractor(sequence);
//                noteExtractor.renderSequence();
//
//                noteExtractor.transposeSequence();
//                ArrayList<MidiNote> midiNotes = noteExtractor.simpleToMidiNotes(noteExtractor.getSimpleOnNotes());
//                this.midiNotes = midiNotes;
//                this.offNotes = noteExtractor.simpleToMidiNotes(noteExtractor.getSimpleOffNotes());
//                this.sequence = noteExtractor.getSequence();
//            }
//            catch (MidiFileLoaderException e) {
//                e.printStackTrace();
//                throw new CommandLineException("Unable to load midi file");
//            }
//        }
//
//        if(cmd.hasOption("o")) {
//            String path = cmd.getOptionValue("o");
//            if(Validator.isValidPath(path)) {
//                outputFilePath = path;
//            }
//            else {
//                throw new CommandLineException("Output file path is not valid: " + cmd.getOptionValue("o"));
//            }
//        }
//
//        if(cmd.hasOption("b")) {
//            if(Validator.isValidImageFile(cmd.getOptionValue("b"))) {
//                background = new File(cmd.getOptionValue("b"));
//            }
//            else {
//                throw new CommandLineException("Unable to load background image: " + cmd.getOptionValue("b"));
//            }
//        }
//
//        if(cmd.hasOption("a")) {
//            if(Validator.isValidFile(cmd.getOptionValue("a"))) {
//                audio = new File(cmd.getOptionValue("a"));
//            }
//            else {
//                System.out.println("Invalid audio file: " + cmd.getOptionValue("a"));
//            }
//        }
//        else {
//            audioFromMidi = true;
//        }
//
//        if(audioFromMidi) {
//            new VideoParts(
//                    midiFile, sequence, midiNotes, offNotes, background, outputFilePath);
//        }
//        else {
//            new VideoParts(
//                    midiFile, sequence, midiNotes, offNotes, background, audio, outputFilePath);
//        }
//    }
//}
