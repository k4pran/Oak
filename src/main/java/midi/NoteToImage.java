package midi;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Various methods for extracting information from midi notes.
 */

public class NoteToImage {

    public static ArrayList<BufferedImage> mapNotesToImages(ArrayList<MidiNote> notes) {
        ArrayList<BufferedImage> ocarinaSprites = new ArrayList<>();

        System.out.println("Mapping notes to images...");
        for(MidiNote note : notes) {
            Integer noteValue = note.getNoteValue();
            String mappedNote = MidiUtils.formatNoteName(noteValue);
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
}
