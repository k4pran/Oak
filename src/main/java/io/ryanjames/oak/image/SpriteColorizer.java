package io.ryanjames.oak.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

public final class SpriteColorizer {

    private static final int GREY_RGB = new Color(128, 128, 128).getRGB();

    private SpriteColorizer() {}

    public static BufferedImage colorSprite(BufferedImage sprite, int targetColor) {
        int width = sprite.getWidth();
        int height = sprite.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = sprite.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                if (isGrey(argb)) {
                    int newArgb = (alpha << 24) | (targetColor & 0x00FFFFFF);
                    sprite.setRGB(x, y, newArgb);
                }
            }
        }

        return sprite;
    }

    public static BufferedImage removeWhiteBackground(BufferedImage sprite) {
        int width = sprite.getWidth();
        int height = sprite.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = sprite.getRGB(x, y);
                if (isWhite(argb)) {
                    sprite.setRGB(x, y, 0);
                }
            }
        }

        return sprite;
    }

    private static boolean isGrey(int argb) {
        return (argb & 0x00FFFFFF) == (GREY_RGB & 0x00FFFFFF);
    }

    private static boolean isWhite(int argb) {
        return (argb & 0x00FFFFFF) == 0x00FFFFFF;
    }
}

