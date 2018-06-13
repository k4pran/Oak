import imagemod.ImageTransform;
import imagemod.SpriteRendering;
import me.tongfei.progressbar.ProgressBar;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Responsible for constructing a frame template for each frame to be used in the video.
 * Sprites are the name given to a single ocarina image.
 * A composite of these sprites along with background, titles etc make up a video frame.
 * The first frame (title frame) of the tutorial holds the title so will contain a row
 * less and the rows/cols in the last frame can vary.
 */

public class ImageFactory {

    //================================================================================
    // Properties
    //================================================================================

    private ProgressBar progressBar;

    private static int WIDTH = 800;
    private static int HEIGHT = 600;

    private Settings settings;
    private VideoParts videoParts;
    private int rows;
    private int cols;
    private int spritesPerPage;
    private int titleFrameCount;
    private int lastFrameCount;

    private ArrayList<BufferedImage> introMessages;

    //================================================================================
    // Constructors
    //================================================================================

    public ImageFactory(Settings settings, VideoParts videoParts) {
        this.settings = settings;
        this.videoParts = videoParts;
        this.rows = settings.getRows();
        this.cols = settings.getCols();
    }

    //================================================================================
    // General methods
    //================================================================================

    public ArrayList<BufferedImage> createImages(ArrayList<BufferedImage> sprites) {

        calculateSpriteCounts(sprites);
        int expectedImages = sprites.size() + 2;

        progressBar = new ProgressBar("Creating frames", sprites.size());
        progressBar.start();
        if (settings.outputPdf()) {
            ArrayList<BufferedImage> pdfImages = coordinatePdfCreation(sprites);
            PdfOutput pdfOutput = new PdfOutput("test", pdfImages);
            pdfOutput.writePdf();
        }
        if (settings.outputVid()) {
            ArrayList<BufferedImage> videoImages = coordinateVideoCreation(sprites);
            progressBar.stop();
            int actualFrames = videoImages.size();

            if (expectedImages == actualFrames) {
                ArrayList<BufferedImage> combined = new ArrayList<>(introMessages);
                combined.addAll(videoImages);

                return combined;
            }
            else {
                throw new FrameConstructionException("Expected images from sprites does not match actual images created");
            }
        }
        else {
            System.exit(0);
        }
        return sprites;
    }

    private void calculateSpriteCounts(ArrayList<BufferedImage> sprites) {
        this.spritesPerPage = rows * cols;
       this.titleFrameCount = sprites.size() >= (rows - 1) * cols ?
                (rows - 1) * cols : sprites.size();
        this.lastFrameCount = (sprites.size() - titleFrameCount) % spritesPerPage == 0 ?
                spritesPerPage : (sprites.size() - titleFrameCount) % spritesPerPage;
    }

    private ArrayList<BufferedImage> coordinatePdfCreation(ArrayList<BufferedImage> sprites) {
        ArrayList<BufferedImage> finishedFrames = new ArrayList<>();
        ArrayList<BufferedImage> spriteSubArr;

        // FIRST
        spriteSubArr = new ArrayList<>(sprites.subList(0, titleFrameCount));
        finishedFrames.add(createForPdf(spriteSubArr, true, false));

        // MIDDLE
        int lowerIndex = titleFrameCount;
        int upperIndex = titleFrameCount + spritesPerPage;

        while(upperIndex <= sprites.size() - lastFrameCount) {
            spriteSubArr = new ArrayList<>(sprites.subList(lowerIndex, upperIndex));
            finishedFrames.add(createForPdf(spriteSubArr, false, false));
            lowerIndex += spritesPerPage;
            upperIndex += spritesPerPage;
        }

        // LAST
        spriteSubArr = new ArrayList<>(sprites.subList(
                sprites.size() - lastFrameCount, sprites.size()));
        finishedFrames.add(createForPdf(spriteSubArr, false, true));

        return finishedFrames;
    }

    private ArrayList<BufferedImage> coordinateVideoCreation(ArrayList<BufferedImage> sprites) {
        ArrayList<BufferedImage> finishedFrames = new ArrayList<>();
        ArrayList<BufferedImage> spriteSubArr;

        // FIRST
        spriteSubArr = new ArrayList<>(sprites.subList(0, titleFrameCount));
        sprites.removeAll(spriteSubArr);
        finishedFrames.addAll(createForVideo(spriteSubArr, true, false,
                SpriteRendering.createPreviewSprite(sprites.get(titleFrameCount), settings.getPreviewNoteColor())));

        // MIDDLE
        while(sprites.size() >  lastFrameCount) {
            spriteSubArr = new ArrayList<>(sprites.subList(0, spritesPerPage));
            finishedFrames.addAll(createForVideo(spriteSubArr, false, false,
                    SpriteRendering.createPreviewSprite(sprites.get(spritesPerPage), settings.getPreviewNoteColor())));
            sprites.removeAll(spriteSubArr);
        }

        // LAST
        spriteSubArr = new ArrayList<>(sprites.subList(0, sprites.size()));
        sprites.removeAll(spriteSubArr);
        finishedFrames.addAll(createForVideo(spriteSubArr, false, true,
                SpriteRendering.createEmptySprite(spriteSubArr.get(0))));

        return finishedFrames;
    }

    private BufferedImage createForPdf(ArrayList<BufferedImage> sprites, boolean isTitle, boolean isLast) {

        BufferedImage img;

        if(isTitle) {
            introMessages = ImageTransform.scaleAll(addMessages(sprites, true), WIDTH, HEIGHT);
        }
        img = addForeGround(sprites, isTitle, isLast);
        img = addBackground(img);
        img = addTextToImage(img, isTitle);
        img = ImageTransform.scale(img, WIDTH, HEIGHT);

        return img;
    }

    private ArrayList<BufferedImage> createForVideo(ArrayList<BufferedImage> sprites, boolean isTitle, boolean isLast,
                                                    BufferedImage previewSprite) {


        ArrayList<BufferedImage> processed;

        if(isTitle) {
            introMessages = ImageTransform.scaleAll(addMessages(sprites, true), WIDTH, HEIGHT);
        }

        processed = addForegrounds(sprites, isTitle, isLast);
        processed = addPreviewPanel(processed, previewSprite);
        processed = addBackgrounds(processed);
        processed = addTextToImages(processed, isTitle, isLast);

        progressBar.stepBy(sprites.size());
        return processed;
    }

    private BufferedImage addForeGround(ArrayList<BufferedImage> sprites, boolean isTitle, boolean isLast) {

        ForegroundFactory foregroundFactory = new ForegroundFactory(
                settings.getNoteOnColor().getRGB(), settings.getNoteOffColor().getRGB(), isTitle, isLast);
        int rows = this.rows;
        return foregroundFactory.createForeGround(sprites, rows, cols);
    }

    private ArrayList<BufferedImage> addForegrounds(
            ArrayList<BufferedImage> sprites, boolean isTitle, boolean isLast) {

        ForegroundFactory foregroundFactory = new ForegroundFactory(
                settings.getNoteOnColor().getRGB(), settings.getNoteOffColor().getRGB(),
                isTitle, isLast, true, WIDTH, HEIGHT);
        int rows = this.rows;
        return foregroundFactory.createForegrounds(sprites, rows, cols);
    }

    private ArrayList<BufferedImage> addPreviewPanel(ArrayList<BufferedImage> images,
                                                     BufferedImage previewSprite) {

        ArrayList<BufferedImage> processed = new ArrayList<>();
        for(BufferedImage image : images) {
            PreviewPanelFactory previewPanelFactory = new PreviewPanelFactory(
                    new Color(0, 255, 255, 50), previewSprite, rows, cols);
            processed.add(previewPanelFactory.addPreviewPanel(image));
        }
        return processed;
    }

    public BufferedImage addBackground(BufferedImage image) {
        return ImageTransform.addBackground(image, videoParts.getBackground());
    }

    public ArrayList<BufferedImage> addBackgrounds(ArrayList<BufferedImage> images) {

        ArrayList<BufferedImage> processed = new ArrayList<>();
        for(BufferedImage image : images) {
            processed.add(ImageTransform.addBackground(image, videoParts.getBackground()));
        }
        return  processed;
    }

    public BufferedImage addTextToImage(BufferedImage image, boolean isTitle) {
        if(isTitle) {
            TextFactory.addTitle(image, CustomText.getTitleText(), rows + 1);
        }
        return image;
    }

    public ArrayList<BufferedImage> addTextToImages(ArrayList<BufferedImage> images, boolean isTitle,
                                                    boolean isLast) {

        for(BufferedImage image : images) {
            if(isTitle) {
                TextFactory.addTitle(image, CustomText.getTitleText(), rows + 1);
            }
            if(!isLast) {
                TextFactory.addText(image, CustomText.getPreviewText(), rows + 1);
            }
        }
        return images;
    }

    private ArrayList<BufferedImage> addMessages(ArrayList<BufferedImage> sprites, boolean intro) {

        BufferedImage templateImg;
        ArrayList<BufferedImage> spritesCopy = new ArrayList<>();

        for(BufferedImage sprite : sprites) {
            spritesCopy.add(ImageTransform.copyImage(sprite));
        }

        for(BufferedImage sprite : spritesCopy) {
            SpriteRendering.colorSprite(sprite, settings.getNoteOffColor().getRGB());
            SpriteRendering.addTransparency(sprite);
        }
        templateImg = ImageTransform.stitchImages(spritesCopy, rows, cols);
        if(intro) {
            templateImg = ImageTransform.padImageTop(templateImg, sprites.get(0).getHeight());
            TextFactory.addTitle(templateImg, CustomText.getTitleText(), rows + 1);
        }
        else {
            templateImg = ImageTransform.padImageBottom(templateImg, sprites.get(0).getHeight());
        }
        templateImg = ImageTransform.addBackground(templateImg, videoParts.getBackground());

        ArrayList<BufferedImage> processed = new ArrayList<>();
        ArrayList<String> messages = intro ? CustomText.getIntroText() : CustomText.getOutroText();
        for(String text : messages) {
            BufferedImage copy = ImageTransform.copyImage(templateImg);
            CustomText.setText(CustomText.getGeneralText(), text);
            TextFactory.addText(copy, CustomText.getGeneralText(), rows + 1);
            processed.add(copy);
        }
        return processed;
    }
}
