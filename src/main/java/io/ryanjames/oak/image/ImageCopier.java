package io.ryanjames.oak.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageCopier {

    private ImageCopier() {}

    /**
     * Makes a copy of an image
     * @param image
     * @return copy of an image
     */
    public static BufferedImage copyImage(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = copy.createGraphics();
        gfx.drawImage(image, 0, 0, null);
        return copy;
    }
}
