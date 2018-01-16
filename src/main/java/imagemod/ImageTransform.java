package imagemod;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageTransform {

    //================================================================================
    // Static methods
    //================================================================================

    /**
     * Stitches images together in order row by row left to right and pads finished image
     * with transparent padded images if necessary to equal rows * cols.
     * @param images
     * @param rows
     * @param cols
     * @return stitched image
     * @throws ImageTransformException
     */
    public static BufferedImage stitchImages(ArrayList<BufferedImage> images, int rows, int cols)
            throws ImageTransformException {
        ArrayList<BufferedImage> imgParts = padImages(images, rows, cols);
        int partWidth = imgParts.get(0).getWidth();
        int partHeight = imgParts.get(0).getHeight();

        BufferedImage stitchedImage = new BufferedImage(
                partWidth * cols,
                partHeight * rows,
                BufferedImage.TYPE_INT_ARGB
        );

        int imgNum = 0;
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                stitchedImage.createGraphics().drawImage(imgParts.get(imgNum),
                        imgParts.get(imgNum).getWidth() * j, imgParts.get(imgNum).getHeight() * i, null);
                imgNum++;
            }
        }
        return stitchedImage;
    }

    /**
     *
     * @param images
     * @param rows
     * @param cols
     * @return images that were passed in padded with blank images to fit dimensions.
     * @throws ImageTransformException
     */
    public static ArrayList<BufferedImage> padImages(ArrayList<BufferedImage> images, int rows, int cols)
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
                background = scale(background, image.getWidth(), image.getHeight());
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
    public static ArrayList<BufferedImage> scaleAll(ArrayList<BufferedImage> imgs, int newWidth, int newHeight) {
        ArrayList<BufferedImage> scaledImgs = new ArrayList<>();
        for(BufferedImage img : imgs) {
            scaledImgs.add(scale(img, newWidth, newHeight));
        }
        return scaledImgs;
    }

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
