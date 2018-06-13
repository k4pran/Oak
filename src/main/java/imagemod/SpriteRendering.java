package imagemod;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpriteRendering {

    public static BufferedImage addTransparency(BufferedImage image) {
        Painter painter = new Painter(image, 0,
                0x0, 0x000011);
        painter.toggleTransparentBrush();
        painter.setInitPixel(new Pixel(image.getWidth() - 1, image.getHeight() - 1));
        return painter.renderImage();
    }

    public static BufferedImage colorSprite(BufferedImage sprite, int color) {
        Painter painter = new Painter(sprite, color,0x0, 0x000011);
        painter.setInitPixel(new Pixel(sprite.getWidth() / 2, sprite.getHeight() / 2));
        painter.renderImage();
        return painter.getImage();
    }

    public static BufferedImage createPreviewSprite(BufferedImage sprite, Color color) {
        sprite = SpriteRendering.addTransparency(sprite);
        sprite = SpriteRendering.colorSprite(sprite, color.getRGB());
        return sprite;
    }

    public static BufferedImage createEmptySprite(BufferedImage sprite) {
        BufferedImage emptySpriteImg = new BufferedImage(sprite.getWidth(), sprite.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = emptySpriteImg.createGraphics();
        gfx.setColor(Color.white);
        gfx.drawRect(0, 0, emptySpriteImg.getWidth(), emptySpriteImg.getHeight());
        return emptySpriteImg;
    }
}

