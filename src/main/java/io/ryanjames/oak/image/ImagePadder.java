package io.ryanjames.oak.image;

import java.awt.image.BufferedImage;
import java.util.List;

public class ImagePadder {

    private ImagePadder() {}

    /**
     *
     * @param images
     * @param rows
     * @param cols
     * @return images that were passed in padded with blank images to fit dimensions.
     * @throws ImageTransformException
     */
    public static List<BufferedImage> padImages(List<BufferedImage> images, int rows, int cols)
            throws ImageTransformException {
        int imgPartsCount = rows * cols;
        int imgPaddingCount = imgPartsCount - images.size();

        if(imgPaddingCount > 0) {
            int imgwidth = images.get(0).getWidth();
            int imgHeight = images.get(0).getHeight();

            for(int i = 0; i < imgPaddingCount; i++) {
                BufferedImage pad = new BufferedImage(imgwidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
                images.add(pad);
            }
        }
        return images;
    }

    /**
     * Pads top of the image equal to width of image and specified height.
     * @param image
     * @param height
     * @return padded image
     */
    public static BufferedImage padImageTop(BufferedImage image, int height) {
        BufferedImage padding = new BufferedImage(image.getWidth(), height, BufferedImage.TYPE_INT_ARGB);

        BufferedImage compositeImage = new BufferedImage(image.getWidth(), image.getHeight() + height,
                BufferedImage.TYPE_INT_ARGB);
        compositeImage.createGraphics().drawImage(padding, 0, 0, null);
        compositeImage.createGraphics().drawImage(image, 0, height, null);
        return compositeImage;
    }

    /**
     * Pads bottom of the image equal to width of image and specified height.
     * @param image
     * @param height
     * @return padded image
     */
    public static BufferedImage padImageBottom(BufferedImage image, int height) {
        BufferedImage padding = new BufferedImage(image.getWidth(), height, BufferedImage.TYPE_INT_ARGB);

        BufferedImage compositeImage = new BufferedImage(image.getWidth(), image.getHeight() + height,
                BufferedImage.TYPE_INT_ARGB);
        compositeImage.createGraphics().drawImage(image, 0, 0, null);
        compositeImage.createGraphics().drawImage(padding, 0, image.getHeight(), null);
        return compositeImage;
    }
}
