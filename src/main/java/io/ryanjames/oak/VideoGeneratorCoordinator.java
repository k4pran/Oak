package io.ryanjames.oak;

import io.ryanjames.oak.config.GlobalConfig;
import io.ryanjames.oak.midi.NoteExtractor;
import io.ryanjames.oak.midi.NoteToImage;
import io.ryanjames.oak.video.FFMpeg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class VideoGeneratorCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(VideoGeneratorCoordinator.class);
    private static final double SECOND_AS_MS = 1000.;

    private final GlobalConfig globalConfig;

    @Inject
    public VideoGeneratorCoordinator(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void generateVideo(String inputFile) {

        ArrayList<BufferedImage> imageFrames = createImageFrames();

        // Add frame duration at beginning (before first note plays but image is displayed()

        double initialFrameDuration = 2000.;

        ArrayList<Double> frameDurations = getFrameDurations();
        frameDurations.addFirst(initialFrameDuration);

        if (frameDurations.size() < imageFrames.size()) { // TODO this is a hack to fix a bug where the last frame duration is missing
            LOG.warn("Frame durations list is shorter than image frames list. Adding last frame duration manually.");
            frameDurations.add(2000.); // Add 2 seconds for the last frame duration
        }

        ArrayList<BufferedImage> videoFrames = createVideoFrames(imageFrames, frameDurations);

        outputVideo(videoFrames, frameDurations);

        cleanUp();
    }

    private ArrayList<BufferedImage> createImageFrames() {
        // Get image sprites
        ArrayList<BufferedImage> ocarinaSprites = NoteToImage.mapNotesToImages(globalConfig.videoConfig().getMidiNotes());

        // Construct image frame
        ImageFactory imageFactory = new ImageFactory(globalConfig.videoConfig());
        return imageFactory.createImages(ocarinaSprites);
    }

    private ArrayList<Double> getFrameDurations() {
        ArrayList<Double> frameDurations = new ArrayList<>();
        VideoConfig videoConfig = globalConfig.videoConfig();
        // Intro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getIntroText().size(),
                SECOND_AS_MS * videoConfig.getMidiFile().getTicksInMs()));

        // Played note frame durations
        frameDurations.addAll(NoteExtractor.extractDurations(
                videoConfig.getMidiNotes(), videoConfig.getOffNotes(), videoConfig.getMidiFile().getTicksInMs()));

        // Outro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getOutroText().size(),
                SECOND_AS_MS * videoConfig.getMidiFile().getTicksInMs()));
        return frameDurations;
    }

    private ArrayList<BufferedImage> createVideoFrames(
            ArrayList<BufferedImage> imageFrames, ArrayList<Double> frameDurations) {
        return VideoFactory.getVideoFrames(imageFrames, frameDurations, globalConfig.videoConfig().getFramerate());
    }

    private void outputVideo(ArrayList<BufferedImage> videoFrames, ArrayList<Double> frameDurations) {
        FFMpeg ffMpeg = new FFMpeg();
        VideoConfig videoConfig = globalConfig.videoConfig();

        double audioOffset = videoConfig.getAudioOffset();
        for(int i = 0; i < CustomText.getIntroText().size(); i++) {
            audioOffset += frameDurations.get(i) / SECOND_AS_MS;
        }
        ffMpeg.outputTutorial(videoFrames, videoConfig.getOutputFilePath(), videoConfig.getAudio().getAbsolutePath(),
                videoConfig.getFramerate(), audioOffset);
    }

    private static void cleanUp() {
        String wavPath = "src/main/resources/temp/temp.wav";
        File file = new File(wavPath);
        if(file.delete()) {
            LOG.info("Temporary audio file {} deleted", wavPath);
        }
    }
}
