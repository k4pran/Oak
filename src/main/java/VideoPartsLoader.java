import midi.MidiFile;
import midi.MidiFileLoaderException;
import midi.MidiToWavRenderer;
import org.apache.commons.cli.CommandLine;
import validation.Validator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VideoPartsLoader {

    private MidiFile midiFile;
    private File background;
    private File audio;
    private ArrayList<File> preMainVidFrames;
    private ArrayList<File> postMainVidFrames;
    private String outputFilePath;
    private String VideoTitle;

    private CommandLine cmd;

    public VideoPartsLoader(CommandLine cmd) throws CommandLineException {
        this.preMainVidFrames = new ArrayList<>();
        this.postMainVidFrames = new ArrayList<>();
        this.cmd = cmd;
        Conductor.setVideoParts(load());
    }

    private VideoParts load() throws CommandLineException {
        if(cmd.hasOption("i")) {
            String midiPath = cmd.getOptionValue("i");
            try {
                midiFile = new MidiFile(new File(midiPath));
            }
            catch (MidiFileLoaderException e) {
                e.printStackTrace();
                throw new CommandLineException("Unable to load midi file");
            }
        }

        if(cmd.hasOption("o")) {
            String path = cmd.getOptionValue("o");
            if(Validator.isValidPath(path)) {
                outputFilePath = path;
            }
            else {
                throw new CommandLineException("Output file path is not valid");
            }
        }

        if(cmd.hasOption("b")) {
            if(Validator.isValidImageFile(cmd.getOptionValue("b"))) {
                background = new File(cmd.getOptionValue("b"));
            }
            else {
                throw new CommandLineException("Unable to load background image");
            }
        }

        if(cmd.hasOption("a")) {
            if(Validator.isValidFile(cmd.getOptionValue("a"))) {
                audio = new File(cmd.getOptionValue("a"));
            }
            else {
                System.out.println("Invalid audio");
            }
        }
        else {
            try {
                String tempAudioPath = "src/main/resources/temp/temp.wav";
                MidiToWavRenderer wavRenderer = new MidiToWavRenderer();
                wavRenderer.createWavFile(
                        new File("src/main/resources/sounds/ocarina.sf2"),
                        midiFile.getFile(),
                        new File(tempAudioPath));
                audio = new File(tempAudioPath);
            }
            catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
                System.out.println("Unable to render wav");
            }
        }

        if(cmd.hasOption("pre")) {
            String[] preFrames = cmd.getOptionValues("pre");
            for(String preFrame : preFrames) {
                if(Validator.isValidFile(preFrame)) {
                    preMainVidFrames.add(new File(preFrame));
                }
                else {
                    throw new CommandLineException("Unable to load pre-video frames");
                }
            }
        }

        if(cmd.hasOption("post")) {
            String[] postFrames = cmd.getOptionValues("post");
            for(String postFrame : postFrames) {
                if(Validator.isValidFile(postFrame)) {
                    postMainVidFrames.add(new File(postFrame));
                }
                else {
                    throw new CommandLineException("Unable to load pre-video frames");
                }
            }
        }
        return new VideoParts(
                midiFile, background, audio, preMainVidFrames,
                postMainVidFrames, outputFilePath);
    }
}
