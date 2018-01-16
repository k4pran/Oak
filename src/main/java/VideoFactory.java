import me.tongfei.progressbar.ProgressBar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class VideoFactory {

    private static String outputDir = "/Users/ryan/Documents/Ocarina/Temp/";
    private static ArrayList<BufferedImage> frames = new ArrayList<>();

    public static ArrayList<BufferedImage> getVideoFrames(ArrayList<BufferedImage> imageStills, ArrayList<Double> frameDurations,
                                                          int framerate, int introCount, int outroCount) {
        int imageIndex = 0;

        for(; imageIndex < introCount; imageIndex++) {
            for(int j = 0; j < Math.rint((framerate * frameDurations.get(imageIndex)) / 1000); j++) {
                frames.add(imageStills.get(imageIndex));
            }
        }

        for(; imageIndex < imageStills.size() - outroCount; imageIndex++) {
            for(int j = 0; j < Math.rint((framerate * frameDurations.get(imageIndex)) / 1000); j++) {
                frames.add(imageStills.get(imageIndex));
            }
        }

        for(; imageIndex < imageStills.size(); imageIndex++) {
            for(int j = 0; j < Math.rint((framerate * frameDurations.get(imageIndex)) / 1000); j++) {
                frames.add(imageStills.get(imageIndex));
            }
        }
        return frames;
    }
}