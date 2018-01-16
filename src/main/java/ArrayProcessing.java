import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ArrayProcessing {

    /**
     * @param source
     * @return a new ArrayList containing a copy of the source ArrayList's values.
     */
    public static ArrayList<BufferedImage> copyArray(ArrayList<BufferedImage> source) {
        ArrayList<BufferedImage> destination = new ArrayList<>();
        for(BufferedImage image : source) {
            destination.add(image);
        }
        return destination;
    }
}