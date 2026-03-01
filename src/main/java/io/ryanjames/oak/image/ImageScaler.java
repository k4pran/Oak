package io.ryanjames.oak.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageScaler {

    private ImageScaler() {}

    /**
     * Simply scales image to new dimensions
     * @param img
     * @param newWidth
     * @param newHeight
     * @return scaled image
     */
    public static BufferedImage scale(BufferedImage img, int newWidth, int newHeight) {
        BufferedImage scaledImg = null;
        if (img != null) {
            scaledImg = new BufferedImage(newWidth, newHeight, img.getType());
            Graphics2D g2 = scaledImg.createGraphics();
            g2.drawImage(img, 0, 0, newWidth, newHeight, null);
            g2.dispose();
        }
        return scaledImg;
    }

    /**
     * Scales all images to new width and height
     * @param imgs
     * @param newWidth
     * @param newHeight
     * @return scaled images
     */
    public static List<BufferedImage> scaleAll(List<BufferedImage> imgs, int newWidth, int newHeight) {
        List<BufferedImage> scaledImgs = new ArrayList<>();
        for(BufferedImage img : imgs) {
            scaledImgs.add(scale(img, newWidth, newHeight));
        }
        return scaledImgs;
    }
}