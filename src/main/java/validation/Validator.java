package validation;

import midi.MidiNote;

import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

public class Validator {

    public static boolean isValidDirectory(String url) {
        Path path = FileSystems.getDefault().getPath(url);
        String absPath = path.toAbsolutePath().toString();
        try {
            File dir = path.toFile();
            if (dir.isDirectory()) {
                System.out.println("Directory: '" + absPath + "' is a valid directory.");
                return true;
            }
        }
        catch (UnsupportedOperationException | SecurityException e) {
            System.out.println("Unable to read directory: " + absPath + " \n\t" + e);
            return false;
        }
        System.out.println("Directory: '" + absPath + "' is NOT a directory.");
        return false;
    }

    public static boolean isValidFile(String url) {
        File file = new File(url);
        return file.exists() && file.isFile();
    }

    public static boolean isValidFile(File file) {
        return file.exists() && file.isFile();
    }

    public static boolean isValidMidiFile(String url) {
        if(!isValidFile(url)) {
            return false;
        }
        try{
            File file = new File(url);
            MidiSystem.getSequence(file);
            return true;
        }
        catch(IOException | InvalidMidiDataException e) {
            return false;
        }
    }

    public static boolean isValidImageFile(String url) {
        File file = new File(url);
        try {
            if(ImageIO.read(file) != null) {
                return true;
            }
        }
        catch(IllegalArgumentException | IOException e) {
            return false;
        }
        return false;
    }

    public static boolean isValidPath(String url) {
        File file = new File(url);
        if(file.getParentFile().exists() && file.getParentFile().canWrite()) {
            return true;
        }
        return false;
    }

    public static boolean isPolyphonic(ArrayList<MidiNote> onNotes) {
        double currentTick = -1;
        for(MidiNote note : onNotes) {
            if(note.getTick() == currentTick) {
                return true;
            }
            currentTick = note.getTick();
        }
        return false;
    }
}
