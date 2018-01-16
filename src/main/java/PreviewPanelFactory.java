
import imagemod.Painter;
import imagemod.Pixel;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Responsible for rendering the bottom row of the video which includes a preview message, text and
 * a note which is a preview of the first note on the next page.
 * Note: panel's width is always the width of image and panel's height is height of a row i.e of a single sprite;
 */

public class PreviewPanelFactory {
    private Color panelTint;
    private BufferedImage previewSprite;
    int rows;
    int cols;

    public PreviewPanelFactory(Color panelTint, BufferedImage previewSprite, int rows, int cols) {
        this.panelTint = panelTint;
        this.previewSprite = addTransparency(previewSprite);
        this.rows = rows;
        this.cols = cols;
    }

    public BufferedImage addPreviewPanel(BufferedImage image) {
        int panelWidth = image.getWidth();
        int panelHeight = previewSprite.getHeight();
        int startPosX = 0;
        int startPosY = image.getHeight() - panelHeight;

        BufferedImage previewPanel = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics prevGfx = previewPanel.createGraphics();
        prevGfx.setColor(panelTint);
        prevGfx.fillRect(0, 0, panelWidth, panelHeight);

        Graphics compGfx = image.createGraphics();
        compGfx.drawImage(previewPanel, startPosX, startPosY, null);
        compGfx.drawImage(previewSprite, previewSprite.getWidth() * (cols - 1), startPosY, null);
        return image;
    }

    private BufferedImage addTransparency(BufferedImage image) {
        Painter painter = new Painter(image, 0,
                0x0, 0x000011);
        painter.toggleTransparentBrush();
        painter.setInitPixel(new Pixel(0, 0));
        return painter.renderImage();
    }
}
