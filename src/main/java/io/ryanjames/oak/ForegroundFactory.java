package io.ryanjames.oak;

import io.ryanjames.oak.image.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
    private boolean scale = false;
    private int width;
    private int height;

    List<BufferedImage> foregrounds;

    public ForegroundFactory(int noteOnColor, int noteOffColor, boolean isFirstFrame, boolean isLastFrame) {
        foregrounds = new ArrayList<>();
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.isFirstFrame = isFirstFrame;
        this.isLastFrame = isLastFrame;
    }

    public ForegroundFactory(int noteOnColor, int noteOffColor, boolean isFirstFrame, boolean isLastFrame, boolean scale,
                             int width, int height) {
        foregrounds = new ArrayList<>();
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.isFirstFrame = isFirstFrame;
        this.isLastFrame = isLastFrame;
        this.scale = scale;
        this.width = width;
        this.height = height;
    }

    public BufferedImage createForeGround(List<BufferedImage> sprites, int rows, int cols) {

        List<BufferedImage> foregroundParts;
        if (isFirstFrame) {
            rows -= 1;
        }
        foregroundParts = renderInactiveSprites(sprites, noteOffColor);
        return stitchSprites(renderInactiveSprites(foregroundParts, noteOffColor), rows, cols);
    }

    public List<BufferedImage> createForegrounds(List<BufferedImage> sprites, int rows, int cols) {

        List<BufferedImage> processed = new ArrayList<>();
        List<BufferedImage> foregroundParts;
        BufferedImage tmp;
        int spriteCount = sprites.size();

        if(isFirstFrame) {
            rows -= 1;
            foregroundParts = renderInactiveSprites(sprites, noteOffColor);
            if (scale) {
                processed.add(ImageScaler.scale(
                        stitchSprites(renderInactiveSprites(foregroundParts, noteOffColor), rows, cols), width, height));
            }
            else {
                processed.add(stitchSprites(renderInactiveSprites(foregroundParts, noteOffColor), rows, cols));
            }
        }

        for(int i = 0; i < spriteCount; i++) {
            foregroundParts = renderInactiveSprites(sprites, noteOffColor);
            foregroundParts.set(i, renderActiveSprite(foregroundParts.get(i), noteOnColor));
            tmp = stitchSprites(foregroundParts, rows, cols);
            if (scale) {
                tmp = ImageScaler.scale(tmp, width, height);
            }
            processed.add(tmp);
        }

        if(isLastFrame) {
            foregroundParts = renderInactiveSprites(sprites, noteOffColor);
            if (scale) {
                processed.add(ImageScaler.scale(stitchSprites(foregroundParts, rows, cols), width, height));
            }
            else {
                processed.add(stitchSprites(foregroundParts, rows, cols));
            }
        }
        this.foregrounds = processed;

        return processed;
    }

    public BufferedImage renderActiveSprite(BufferedImage sprite, int color) {
        sprite = SpriteRendering.colorSprite(sprite, color);
        return SpriteRendering.addTransparency(sprite);
    }

    public List<BufferedImage> renderInactiveSprites(List<BufferedImage> sprites, int color) {
        for(int i = 0; i < sprites.size(); i++) {
            sprites.set(i, SpriteRendering.colorSprite(sprites.get(i), color));
            sprites.set(i, SpriteRendering.addTransparency(sprites.get(i)));
        }
        return sprites;
    }

    public BufferedImage stitchSprites(List<BufferedImage> imageParts, int rows, int cols) {
        if(isFirstFrame) {
            imageParts = ImagePadder.padImages(imageParts, rows, cols);

            BufferedImage stitchedImage = ImageStitcher.stitchImages(imageParts, rows, cols);
            stitchedImage = ImagePadder.padImageBottom(stitchedImage, imageParts.get(0).getHeight());
            return ImagePadder.padImageTop(stitchedImage, imageParts.get(0).getHeight());
        }

        else {
            imageParts = ImagePadder.padImages(imageParts, rows, cols);
            BufferedImage stitchedImage = ImageStitcher.stitchImages(imageParts, rows, cols);
            return ImagePadder.padImageBottom(stitchedImage, imageParts.get(0).getHeight());
        }
    }
}
