package io.ryanjames.oak;

import io.ryanjames.oak.image.Movement;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TextFactory {

    private static final Color OUTLINE_COLOR = new Color(0, 0, 0, 170);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 120);
    private static final int OUTLINE_RADIUS = 1;
    private static final int SHADOW_OFFSET = 2;

    public static BufferedImage addTitle(BufferedImage image, CustomText customText, int rows) {
        Graphics2D gfx = (Graphics2D) image.createGraphics();
        enableTextRendering(gfx);
        gfx.setFont(customText.getFont());
        int lineWidth = image.getWidth() - (image.getWidth() / 10);

        int x = image.getWidth() / 10;
        int y = (image.getHeight() / rows) / 2;

        drawStringMultiLineShadow(gfx, customText, lineWidth,
                Movement.ShiftEast(x, SHADOW_OFFSET), Movement.ShiftSouth(y, SHADOW_OFFSET), x);
        drawStringMultiLineOutline(gfx, customText, lineWidth, x, y, x);
        gfx.setColor(new Color(customText.getColor()));
        drawStringMultiLine(gfx, customText, lineWidth, x, y, x);
        gfx.dispose();

        return image;
    }

    public static BufferedImage addText(BufferedImage image, CustomText customText, int rows) {
        Graphics2D gfx = (Graphics2D) image.createGraphics();
        enableTextRendering(gfx);
        gfx.setFont(customText.getFont());

        int height = image.getHeight() / rows;
        int lineWidth = image.getWidth() - (image.getWidth() / 10);
        int centreW = customText.getText().equalsIgnoreCase("preview note") ?
                image.getWidth() / 5 : image.getWidth() / 10;
        int centreH =  image.getHeight() - (height / 2);

        drawStringMultiLineShadow(gfx, customText, lineWidth, centreW + SHADOW_OFFSET, centreH + SHADOW_OFFSET, centreW);
        drawStringMultiLineOutline(gfx, customText, lineWidth, centreW, centreH, centreW);
        gfx.setColor(new Color(customText.getColor()));
        TextFactory.drawStringMultiLine(gfx, customText, lineWidth, centreW, centreH, centreW);
        gfx.dispose();

        return image;
    }

    public static void drawStringMultiLine(Graphics gfx, CustomText customText, int lineWidth,
                                           int x, int y, int rightBoundary) {
        FontMetrics fontMetrics = gfx.getFontMetrics();

        if(fontMetrics.stringWidth(customText.getText()) < lineWidth) {
            gfx.drawString(customText.getText(), x, y);
        }
        else {
            String[] words = customText.getText().split(" ");
            String currentLine = words[0];
            for(int i = 1; i < words.length; i++) {
                if(fontMetrics.stringWidth(currentLine+words[i]) < lineWidth - rightBoundary) {
                    currentLine += " " + words[i];
                } else {
                    gfx.drawString(currentLine, x, y);
                    y += fontMetrics.getHeight();
                    currentLine = words[i];
                }
            }
            if(currentLine.trim().length() > 0) {
                gfx.drawString(currentLine, x, y);
            }
        }
    }

    private static void enableTextRendering(Graphics2D gfx) {
        gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static void drawStringMultiLineShadow(Graphics2D gfx, CustomText customText, int lineWidth,
                                                  int x, int y, int rightBoundary) {
        gfx.setColor(SHADOW_COLOR);
        drawStringMultiLine(gfx, customText, lineWidth, x, y, rightBoundary);
    }

    private static void drawStringMultiLineOutline(Graphics2D gfx, CustomText customText, int lineWidth,
                                                   int x, int y, int rightBoundary) {
        gfx.setColor(OUTLINE_COLOR);
        for (int dx = -OUTLINE_RADIUS; dx <= OUTLINE_RADIUS; dx++) {
            for (int dy = -OUTLINE_RADIUS; dy <= OUTLINE_RADIUS; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                drawStringMultiLine(gfx, customText, lineWidth, x + dx, y + dy, rightBoundary);
            }
        }
    }
}
