package imagemod;/*
* <h2>Image Fills</h2>
*
* <p>This class includes a number of methods for filling inside areas and outside
* areas of outlines. Some of these methods require coordinates inside an area
* with continuous bounds to work properly, it will perform a check to ensure
* this condition is met and return an exception if not. Some methods fill pixels
* with colors, others fill with transparent pixels.</p>
*
* <p>The boundaryArea can be set to define what value constitutes a boundaryArea.
* It consists of an upperBound and lowerBound part. Any rgb values that are both below the
* upperBound boundaryArea and above the lowerBound boundaryArea is classed as a boundaryArea initPixel.
* The boundaries are inclusive and so values equal to the upperBound or lowerBound boundaries
* are also classed as boundaryArea pixels.</p>
*
* Algorithms used are non-recursive so avoid memory issues and instead builds
* the algorithms around <tt>LinkedList</tt>.
*
* <p>Certain keywords in the methods will hint at their behaviour that you can
* review for reference:
*                         - Outer - Outer area of image.
*                         - Inner - Inner area defined by continuous bounds.
*                         - Color - Colors the pixels.
*                         - Transparent - Renders pixels transparent.
*                         - Level(s) - Targets user-defined boundaryArea level(s).</p>
*
* @author cr0n
* @version 1.0
* @since 2017-10-01
*/

import color.ColorRange;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Paints an image avoiding borders that are defined by borderRange. Can target specific colors using colorRange
 * and can also use a transparent brush to render areas transparent.
 */

public class Painter {

    //================================================================================
    // Properties
    //================================================================================

    private static Queue<Pixel> pixels = new LinkedList();
    private BufferedImage image;
    private Pixel initPixel;
    private int brushColor;
    private boolean transparentBrush = false;
    private ColorRange targetArea;
    private ColorRange boundaryArea;

    //================================================================================
    // Constructors
    //================================================================================

    public Painter(BufferedImage image, int brushColor, int rgbLowerBound, int rgbUpperBound) {
        this.image = image;
        this.targetArea = ColorRange.getFullRange();
        this.boundaryArea = new ColorRange(rgbLowerBound, rgbUpperBound);
        this.brushColor = brushColor;
        this.initPixel = new Pixel(0, 0);
    }

    public Painter(BufferedImage image, int brushColor, int rgbLowerBound, int rgbUpperBound,
                   int targetUpper, int targetLower) {
        this.image = image;
        this.brushColor = brushColor;
        this.initPixel = new Pixel(0, 0);
    }

    //================================================================================
    // General methods
    //================================================================================

    /**
     * <p>Fills area with brushColor keeping inside/outside boundaries. Set starting
     * initPixel outside area for an outer fill or within an enclosed boundaryArea for
     * an inner fill.</p>
    */

    public BufferedImage renderImage() {

        if(image == null) {
            return null;
        }

        // Check if already set
        int currentPixelColor = image.getRGB(this.initPixel.getX(), this.initPixel.getY());
        if(currentPixelColor == brushColor && !transparentBrush) {
            return image;
        }

        pixels.add(new Pixel(this.initPixel.getX(), this.initPixel.getY()));

        // Set initPixel
        renderPixel(initPixel);

        Pixel currentPixel;

        while(!pixels.isEmpty()) {
            currentPixel = pixels.poll();

            // North
            Pixel northPixel = new Pixel(currentPixel.getX(), currentPixel.getY() - 1);
            renderPixel(northPixel);

            // East
            Pixel eastPixel = new Pixel(currentPixel.getX() + 1, currentPixel.getY());
            renderPixel(eastPixel);

            // South
            Pixel southPixel = new Pixel(currentPixel.getX(), currentPixel.getY() + 1);
            renderPixel(southPixel);

            // West
            Pixel westPixel = new Pixel(currentPixel.getX() - 1, currentPixel.getY());
            renderPixel(westPixel);
        }
        return image;
    }

    private void renderPixel(Pixel targetPixel) {
        if(pixelExists(targetPixel)) {
            int pixelColor = image.getRGB(targetPixel.getX(), targetPixel.getY());
            if(!isAlreadySet(pixelColor)) {
                if(isTarget(pixelColor)) {
                    if(!isInBoundary(pixelColor)) {
                        paintPixel(targetPixel);
                    }
                }
            }
        }
    }

    private void paintPixel(Pixel pixel) {
        if(!transparentBrush) {
            image.setRGB(pixel.getX(), pixel.getY(), brushColor);
        }
        else if(transparentBrush) {
            image.setRGB(pixel.getX(), pixel.getY(), 0);
        }
        addPixelToQueue(pixel);
    }

    private void addPixelToQueue(Pixel toAdd) {
        pixels.add(toAdd);
    }

    private boolean pixelExists(Pixel pixel) {
        if(pixel.getX() < 0 || pixel.getY() < 0) {
            return false;
        }
        if(pixel.getX() > (image.getWidth() - 1) || pixel.getY() > (image.getHeight() - 1)) {
            return false;
        }
        return true;
    }

    private boolean isAlreadySet(int targetColor) {
        if(!transparentBrush) {
            return targetColor == brushColor;
        }
        else {
            return ((targetColor >> 24) & 0xFF) == 0;
        }
    }

    private boolean isTarget(int color) {
        return targetArea.isWithinBounds(color);
    }

    private boolean isInBoundary(int color) {
        return boundaryArea.isWithinBounds(color);
    }

    public void toggleTransparentBrush() {
        this.transparentBrush = !transparentBrush;
    }

    //================================================================================
    // Accessors
    //================================================================================

    public BufferedImage getImage() {
        return image;
    }

    public void setInitPixel(Pixel initPixel) {
        this.initPixel = initPixel;
    }

    public void setBrushColor(int brushColor) {
        this.brushColor = brushColor;
    }
}