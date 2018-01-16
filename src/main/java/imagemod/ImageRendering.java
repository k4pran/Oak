package imagemod;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageRendering {

    public static BufferedImage renderTransparent(BufferedImage image) {
        Graphics gfx = image.createGraphics();
        gfx.setColor(new Color(0, 0, 0, 0));
        gfx.fillRect(0, 0, image.getWidth(), image.getHeight());
        return image;
    }
}
