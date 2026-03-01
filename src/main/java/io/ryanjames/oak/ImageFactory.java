package io.ryanjames.oak;

import io.ryanjames.oak.image.*;
import jline.internal.Log;
import me.tongfei.progressbar.ProgressBar;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for constructing a frame template for each frame to be used in the video.
 * Sprites are the name given to a single ocarina image.
 * A composite of these sprites along with background, titles etc make up a video frame.
 * The first frame (title frame) of the tutorial holds the title so will contain a row
 * less and the rows/cols in the last frame can vary.
 */

public class ImageFactory {

    private ProgressBar progressBar;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final VideoConfig videoConfig;
    private final int rows;
    private final int cols;
    private int spritesPerPage;
    private int titleFrameCount;
    private int lastFrameCount;
    private File background;

    private List<BufferedImage> introMessages;

    public ImageFactory(VideoConfig videoConfig, File background) {
        this.videoConfig = videoConfig;
        this.rows = videoConfig.getDims();
        this.cols = videoConfig.getDims();
        this.background = background;
    }

    public List<BufferedImage> createVideoFrames(List<BufferedImage> sprites) {

        calculateSpriteCounts(sprites);
        int expectedImages = sprites.size() + 2;

        progressBar = new ProgressBar("Creating frames", sprites.size());
        progressBar.start();
        List<BufferedImage> videoImages = coordinateVideoCreation(sprites);
        progressBar.stop();
        int actualFrames = videoImages.size();

        if (expectedImages == actualFrames) {
            List<BufferedImage> combined = new ArrayList<>(introMessages);
            combined.addAll(videoImages);

            return combined;
        } else {
            throw new FrameConstructionException("Expected images from sprites does not match actual images created");
        }
    }

    public List<BufferedImage> createDocFrames(List<BufferedImage> sprites) {
        calculateSpriteCounts(sprites);

        Log.info("Creating document frames");
        return coordinatePdfCreation(sprites);
    }

        private void calculateSpriteCounts(List<BufferedImage> sprites) {
        this.spritesPerPage = rows * cols;
        this.titleFrameCount = Math.min(sprites.size(), (rows - 1) * cols);
        this.lastFrameCount = (sprites.size() - titleFrameCount) % spritesPerPage == 0 ?
                spritesPerPage : (sprites.size() - titleFrameCount) % spritesPerPage;
    }

    private List<BufferedImage> coordinatePdfCreation(List<BufferedImage> sprites) {
        List<BufferedImage> finishedFrames = new ArrayList<>();
        List<BufferedImage> spriteSubArr;

        // FIRST
        spriteSubArr = new ArrayList<>(sprites.subList(0, titleFrameCount));
        finishedFrames.add(createForPdf(spriteSubArr, true, false));

        // MIDDLE
        int lowerIndex = titleFrameCount;
        int upperIndex = titleFrameCount + spritesPerPage;

        while (upperIndex <= sprites.size() - lastFrameCount) {
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

    private List<BufferedImage> coordinateVideoCreation(List<BufferedImage> sprites) {
        List<BufferedImage> spriteSubArr;

        // FIRST
        spriteSubArr = new ArrayList<>(sprites.subList(0, titleFrameCount));
        sprites.removeAll(spriteSubArr);
        BufferedImage previewSprite = createNextPreviewSprite(sprites, spriteSubArr.get(0));
        List<BufferedImage> finishedFrames = new ArrayList<>(createForVideo(spriteSubArr, true, false,
                previewSprite));

        // MIDDLE
        while (sprites.size() > lastFrameCount) {
            spriteSubArr = new ArrayList<>(sprites.subList(0, spritesPerPage));
            finishedFrames.addAll(createForVideo(spriteSubArr, false, false,
                    SpriteRendering.createPreviewSprite(sprites.get(spritesPerPage), videoConfig.getPreviewNoteColor())));
            sprites.removeAll(spriteSubArr);
        }

        // LAST
        spriteSubArr = new ArrayList<>(sprites.subList(0, sprites.size()));
        sprites.removeAll(spriteSubArr);
        finishedFrames.addAll(createForVideo(spriteSubArr, false, true,
                SpriteRendering.createEmptySprite(spriteSubArr.get(0))));

        return finishedFrames;
    }

    private BufferedImage createForPdf(List<BufferedImage> sprites, boolean isTitle, boolean isLast) {

        BufferedImage img;

        if (isTitle) {
            introMessages = addMessages(sprites, true);
        }
        img = addForeGround(sprites, isTitle, isLast);
        img = addBackground(img);
        img = addTextToImage(img, isTitle);
        img = ImageScaler.scale(img, WIDTH, HEIGHT);

        return img;
    }

    private List<BufferedImage> createForVideo(List<BufferedImage> sprites, boolean isTitle, boolean isLast,
                                                    BufferedImage previewSprite) {


        List<BufferedImage> processed;

        if (isTitle) {
            introMessages = ImageScaler.scaleAll(addMessages(sprites, true), WIDTH, HEIGHT);
        }

        processed = addForegrounds(sprites, isTitle, isLast);
        processed = addPreviewPanel(processed, previewSprite);
        processed = ImageScaler.scaleAll(processed, WIDTH, HEIGHT);
        processed = addBackgrounds(processed);
        processed = addTextToImages(processed, isTitle, isLast);

        progressBar.stepBy(sprites.size());
        return processed;
    }

    private BufferedImage addForeGround(List<BufferedImage> sprites, boolean isTitle, boolean isLast) {

        ForegroundFactory foregroundFactory = new ForegroundFactory(
                videoConfig.getNoteOnColor().getRGB(), videoConfig.getNoteOffColor().getRGB(), isTitle, isLast);
        int rows = this.rows;
        return foregroundFactory.createForeGround(sprites, rows, cols);
    }

    private List<BufferedImage> addForegrounds(
            List<BufferedImage> sprites, boolean isTitle, boolean isLast) {

        ForegroundFactory foregroundFactory = new ForegroundFactory(
                videoConfig.getNoteOnColor().getRGB(), videoConfig.getNoteOffColor().getRGB(),
                isTitle, isLast);
        int rows = this.rows;
        return foregroundFactory.createForegrounds(sprites, rows, cols);
    }

    private List<BufferedImage> addPreviewPanel(List<BufferedImage> images,
                                                     BufferedImage previewSprite) {

        List<BufferedImage> processed = new ArrayList<>();
        for (BufferedImage image : images) {
            PreviewPanelFactory previewPanelFactory = new PreviewPanelFactory(
                    new Color(0, 255, 255, 50), previewSprite, rows, cols);
            processed.add(previewPanelFactory.addPreviewPanel(image));
        }
        return processed;
    }

    public BufferedImage addBackground(BufferedImage image) {
        return ImageLayering.addBackground(image, background);
    }

    public List<BufferedImage> addBackgrounds(List<BufferedImage> images) {

        List<BufferedImage> processed = new ArrayList<>();
        for (BufferedImage image : images) {
            processed.add(ImageLayering.addBackground(image, background));
        }
        return processed;
    }

    public BufferedImage addTextToImage(BufferedImage image, boolean isTitle) {
        if (isTitle) {
            TextFactory.addTitle(image, CustomText.getTitleText(), rows + 1);
        }
        return image;
    }

    public List<BufferedImage> addTextToImages(List<BufferedImage> images, boolean isTitle,
                                                    boolean isLast) {

        for (BufferedImage image : images) {
            if (isTitle) {
                TextFactory.addTitle(image, CustomText.getTitleText(), rows + 1);
            }
            if (!isLast) {
                TextFactory.addText(image, CustomText.getPreviewText(), rows + 1);
            }
        }
        return images;
    }

    private List<BufferedImage> addMessages(List<BufferedImage> sprites, boolean intro) {

        BufferedImage templateImg;
        List<BufferedImage> spritesCopy = new ArrayList<>();

        for (BufferedImage sprite : sprites) {
            spritesCopy.add(ImageCopier.copyImage(sprite));
        }

        for (BufferedImage sprite : spritesCopy) {
            SpriteRendering.colorSprite(sprite, videoConfig.getNoteOffColor().getRGB());
            SpriteRendering.addTransparency(sprite);
        }
        spritesCopy = ImagePadder.padImages(spritesCopy, rows, cols);
        templateImg = ImageStitcher.stitchImages(spritesCopy, rows, cols);
        templateImg = ImageScaler.scale(templateImg, WIDTH, HEIGHT);
        if (intro) {
            templateImg = ImagePadder.padImageTop(templateImg, templateImg.getHeight() / cols);
            TextFactory.addTitle(templateImg, CustomText.getTitleText(), rows + 1);
        } else {
            templateImg = ImagePadder.padImageBottom(templateImg, templateImg.getHeight() / cols);
        }
        templateImg = ImageLayering.addBackground(templateImg, background);

        List<BufferedImage> processed = new ArrayList<>();
        List<String> messages = intro ? CustomText.getIntroText() : CustomText.getOutroText();
        for (String text : messages) {
            BufferedImage copy = ImageCopier.copyImage(templateImg);
            CustomText.setText(CustomText.getGeneralText(), text);
            TextFactory.addText(copy, CustomText.getGeneralText(), rows + 1);
            processed.add(copy);
        }
        return processed;
    }

    private BufferedImage createNextPreviewSprite(List<BufferedImage> remaining, BufferedImage fallbackSprite) {
        if (remaining == null || remaining.isEmpty()) {
            return SpriteRendering.createEmptySprite(fallbackSprite);
        }
        return SpriteRendering.createPreviewSprite(remaining.get(0), videoConfig.getPreviewNoteColor());
    }
}
