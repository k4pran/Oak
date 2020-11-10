import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class VideoFactory {

    private static final int SECOND_AS_MS = 1000;

    private static final ArrayList<BufferedImage> frames = new ArrayList<>();

    public static ArrayList<BufferedImage> getVideoFrames(ArrayList<BufferedImage> imageStills, ArrayList<Double> frameDurations,
                                                          int framerate) {

        for(int imageIndex = 0; imageIndex < imageStills.size(); imageIndex++) {
            for(int j = 0; j < Math.rint((framerate * frameDurations.get(imageIndex)) / SECOND_AS_MS); j++) {
                frames.add(imageStills.get(imageIndex));
            }
        }
        return frames;
    }
}
