package io.ryanjames.oak.image;

import java.awt.image.BufferedImage;
import java.util.List;

public class ImageStitcher {

    private ImageStitcher() {}

    /**
     * Stitches images together in order row by row left to right and pads finished image
     * with transparent padded images if necessary to equal rows * cols.
     * @param images
     * @param rows
     * @param cols
     * @return stitched image
     * @throws ImageTransformException
     */
    public static BufferedImage stitchImages(List<BufferedImage> images, int rows, int cols)
            throws ImageTransformException {
        int partWidth = images.get(0).getWidth();
        int partHeight = images.get(0).getHeight();

        BufferedImage stitchedImage = new BufferedImage(
                partWidth * cols,
                partHeight * rows,
                BufferedImage.TYPE_INT_ARGB
        );

        int imgNum = 0;
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                stitchedImage.createGraphics().drawImage(images.get(imgNum),
                        images.get(imgNum).getWidth() * j, images.get(imgNum).getHeight() * i, null);
                imgNum++;
            }
        }
        return stitchedImage;
    }
}
