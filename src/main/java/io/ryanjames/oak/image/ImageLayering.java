package io.ryanjames.oak.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLayering {

    private ImageLayering() {}

    /**
     * Adds background to an image then redraws image on top.
     * @param image
     * @param backgroundFile
     * @return new image with background
     */
    public static BufferedImage addBackground(BufferedImage image, File backgroundFile) {
        try {
            BufferedImage background = ImageIO.read(backgroundFile);

            if(image.getWidth() != background.getWidth() || image.getHeight() != background.getHeight()) {
                background = ImageScaler.scale(background, image.getWidth(), image.getHeight());
            }
            BufferedImage composite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D compGfx = composite.createGraphics();
            compGfx.drawImage(background, 0, 0, null);
            compGfx.drawImage(image, 0, 0, null);
            return composite;
        }
        catch (IOException e) {
            throw new ImageProcessingException("Error reading image when adding background");
        }
    }
}
