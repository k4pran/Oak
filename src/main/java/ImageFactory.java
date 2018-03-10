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

    private static int defPixelWidth = 800;
    private static int defPixelHeight = 600;

    private Settings settings;
    private VideoParts videoParts;
    private int rows;
    private int cols;
    private int totalSpriteCount;
    private int spritesPerPage;
    private int titleFrameCount;
    private int lastFrameCount;

    private ArrayList<BufferedImage> introMessages;
    private BufferedImage emptySpriteImg;

    //================================================================================
    // Constructors
    //================================================================================

    public ImageFactory(Settings settings, VideoParts videoParts) {
        this.settings = settings;
        this.videoParts = videoParts;
        this.rows = settings.getROWS();
        this.cols = settings.getCOLS();
    }

    //================================================================================
    // General methods
    //================================================================================

    public ArrayList<BufferedImage> createImages(ArrayList<BufferedImage> sprites) {

        calculateSpriteCounts(sprites);
        int expectedImages = sprites.size() + 2;

        progressBar = new ProgressBar("Creating frames", sprites.size());
        progressBar.start();
        ArrayList<BufferedImage> images = allocateJobs(sprites);
        progressBar.stop();
        int actualFrames = images.size();

        if(expectedImages == actualFrames) {
            ArrayList<BufferedImage> combined = new ArrayList<>(introMessages);
            combined.addAll(images);

            return combined;
        }
        else {
            throw new FrameConstructionException("Expected images from sprites does not match actual images created");
        }
    }

    private void calculateSpriteCounts(ArrayList<BufferedImage> sprites) {
        this.totalSpriteCount = sprites.size();
        this.spritesPerPage = rows * cols;
       this.titleFrameCount = sprites.size() >= (rows - 1) * cols ?
                (rows - 1) * cols : sprites.size();
        this.lastFrameCount = (sprites.size() - titleFrameCount) % spritesPerPage == 0 ?
                spritesPerPage : (sprites.size() - titleFrameCount) % spritesPerPage;
    }

    private ArrayList<BufferedImage> allocateJobs(ArrayList<BufferedImage> sprites) {
        ArrayList<BufferedImage> finishedFrames = new ArrayList<>();
        ArrayList<BufferedImage> spriteSubArr;

        // FIRST

        spriteSubArr = new ArrayList<>(sprites.subList(0, titleFrameCount));
        finishedFrames.addAll(process(spriteSubArr, true, false,
                createPreviewSprite(sprites.get(titleFrameCount))));

        // MIDDLE

        int lowerIndex = titleFrameCount;
        int upperIndex = titleFrameCount + spritesPerPage;

        while(upperIndex <= sprites.size() - lastFrameCount) {
            spriteSubArr = new ArrayList<>(sprites.subList(lowerIndex, upperIndex));
            finishedFrames.addAll(process(spriteSubArr, false, false,
                    createPreviewSprite(sprites.get(upperIndex))));
            lowerIndex += spritesPerPage;
            upperIndex += spritesPerPage;
        }

        // LAST

        spriteSubArr = new ArrayList<>(sprites.subList(
                sprites.size() - lastFrameCount, sprites.size()));
        finishedFrames.addAll(process(spriteSubArr, false, true, createEmptySprite(spriteSubArr.get(0))));

        return finishedFrames;
    }

    private ArrayList<BufferedImage> process(ArrayList<BufferedImage> sprites, boolean isTitle, boolean isLast,
                                             BufferedImage previewSprite) {


        // Foreground
        ArrayList<BufferedImage> processed;

        if(isTitle) {
            introMessages = ImageTransform.scaleAll(processMessages(sprites, true), defPixelWidth, defPixelHeight);
        }

        processed = processForegrounds(sprites, isTitle, isLast);
        processed = processPreviewPanel(processed, previewSprite);
        processed = processBackgrounds(processed);
        processed = processText(processed, isTitle, isLast);

        ArrayList<BufferedImage> p = new ArrayList<>();
        for(BufferedImage img : processed) {
            p.add(ImageTransform.scale(img, defPixelWidth, defPixelHeight));
        }

        progressBar.stepBy(sprites.size());
        return p;
    }

    private ArrayList<BufferedImage> processForegrounds(
            ArrayList<BufferedImage> sprites, boolean isTitle, boolean isLast) {

        ForegroundFactory foregroundFactory = new ForegroundFactory(
                settings.getNoteOnColor().getRGB(), settings.getNoteOffColor().getRGB(),
                isTitle, isLast);
        int rows = this.rows;
        return foregroundFactory.createForegrounds(sprites, rows, cols);
    }

    private ArrayList<BufferedImage> processPreviewPanel(ArrayList<BufferedImage> images,
                                                         BufferedImage previewSprite) {

        ArrayList<BufferedImage> processed = new ArrayList<>();
        for(BufferedImage image : images) {
            PreviewPanelFactory previewPanelFactory = new PreviewPanelFactory(
                    new Color(0, 255, 255, 50), previewSprite, rows, cols);
            processed.add(previewPanelFactory.addPreviewPanel(image));
        }
        return processed;
    }

    ArrayList<BufferedImage> processBackgrounds(ArrayList<BufferedImage> images) {

        ArrayList<BufferedImage> processed = new ArrayList<>();
        for(BufferedImage image : images) {
            processed.add(ImageTransform.addBackground(image, videoParts.getBackground()));
        }
        return  processed;
    }

    ArrayList<BufferedImage> processText(ArrayList<BufferedImage> images, boolean isTitle,
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

    private ArrayList<BufferedImage> processMessages(ArrayList<BufferedImage> sprites, boolean intro) {

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

    private BufferedImage createPreviewSprite(BufferedImage sprite) {
        sprite = SpriteRendering.addTransparency(sprite);
        sprite = SpriteRendering.colorSprite(sprite, settings.getPreviewNoteColor().getRGB());
        return sprite;
    }

    private BufferedImage createEmptySprite(BufferedImage sprite) {
        emptySpriteImg = new BufferedImage(sprite.getWidth(), sprite.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = emptySpriteImg.createGraphics();
        gfx.setColor(Color.white);
        gfx.drawRect(0, 0, emptySpriteImg.getWidth(), emptySpriteImg.getHeight());
        return emptySpriteImg;
    }
}
