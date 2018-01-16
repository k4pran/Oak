import imagemod.ImageTransform;
import imagemod.SpriteRendering;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Constructs the foreground part of the image including padding, coloring, and stitching together of ocarina
 * sprites. Also renders parts of the image transparent to layer image later.
 * @author ryan
 */

public class ForegroundFactory {

    int noteOnColor;
    int noteOffColor;
    private boolean isFirstFrame = false;
    private boolean isLastFrame = false;

    ArrayList<BufferedImage> foregrounds;

    public ForegroundFactory(int noteOnColor, int noteOffColor, boolean isFirstFrame, boolean isLastFrame) {
        foregrounds = new ArrayList<>();
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.isFirstFrame = isFirstFrame;
        this.isLastFrame = isLastFrame;
    }

    public ArrayList<BufferedImage> createForegrounds(ArrayList<BufferedImage> sprites, int rows, int cols) {

        ArrayList<BufferedImage> processed = new ArrayList<>();
        ArrayList<BufferedImage> foregroundParts;
        BufferedImage tmp;
        int spriteCount = sprites.size();

        if(isFirstFrame) {
            rows -= 1;
            foregroundParts = renderInactiveSprites(sprites, noteOffColor);
            processed.add(stitchSprites(renderInactiveSprites(foregroundParts, noteOffColor), rows, cols));
        }

        for(int i = 0; i < spriteCount; i++) {
            foregroundParts = renderInactiveSprites(sprites, noteOffColor);
            foregroundParts.set(i, renderActiveSprite(foregroundParts.get(i), noteOnColor));
            tmp = stitchSprites(foregroundParts, rows, cols);
            processed.add(tmp);
        }

        if(isLastFrame) {
            foregroundParts = renderInactiveSprites(sprites, noteOffColor);
            processed.add(stitchSprites(foregroundParts, rows, cols));
        }
        this.foregrounds = processed;
        return processed;
    }

    public BufferedImage renderActiveSprite(BufferedImage sprite, int color) {
        sprite = SpriteRendering.colorSprite(sprite, color);
        return SpriteRendering.addTransparency(sprite);
    }

    public ArrayList<BufferedImage> renderInactiveSprites(ArrayList<BufferedImage> sprites, int color) {
        for(int i = 0; i < sprites.size(); i++) {
            sprites.set(i, SpriteRendering.colorSprite(sprites.get(i), color));
            sprites.set(i, SpriteRendering.addTransparency(sprites.get(i)));
        }
        return sprites;
    }

    public BufferedImage stitchSprites(ArrayList<BufferedImage> imageParts, int rows, int cols) {
        if(isFirstFrame) {
            BufferedImage stitchedImage = ImageTransform.stitchImages(imageParts, rows, cols);
            stitchedImage = ImageTransform.padImageBottom(stitchedImage, imageParts.get(0).getHeight());
            return ImageTransform.padImageTop(stitchedImage, imageParts.get(0).getHeight());
        }

        else {
            BufferedImage stitchedImage = ImageTransform.stitchImages(imageParts, rows, cols);
            return ImageTransform.padImageBottom(stitchedImage, imageParts.get(0).getHeight());
        }
    }
}