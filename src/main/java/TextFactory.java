import imagemod.Movement;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TextFactory {

    public static BufferedImage addTitle(BufferedImage image, CustomText customText, int rows) {
        Graphics gfx = image.createGraphics();
        gfx.setFont(customText.getFont());
        int lineWidth = image.getWidth() - (image.getWidth() / 10);

        gfx.setColor(new Color(220, 220, 220));
        drawStringMultiLine(gfx, customText, lineWidth, Movement.ShiftEast(image.getWidth() / 10, 1),
                Movement.ShiftSouth((image.getHeight() / rows) / 2, 1),image.getWidth() / 10);
        gfx.setColor(new Color(customText.getColor()));
        drawStringMultiLine(gfx, customText, lineWidth, image.getWidth() / 10,
                (image.getHeight() / rows) / 2, image.getWidth() / 10);

        return image;
    }

    public static BufferedImage addText(BufferedImage image, CustomText customText, int rows) {
        Graphics gfx = image.createGraphics();
        gfx.setFont(customText.getFont());
        gfx.setColor(new Color(customText.getColor()));

        int height = image.getHeight() / rows;
        int lineWidth = image.getWidth() - (image.getWidth() / 10);
        int centreW = customText.getText().equalsIgnoreCase("preview note") ?
                 image.getWidth() / 5 : image.getWidth() / 10;
        int centreH =  image.getHeight() - (height / 2);
        TextFactory.drawStringMultiLine(gfx, customText, lineWidth, centreW, centreH, centreW);

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
}
