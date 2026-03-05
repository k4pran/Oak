package io.ryanjames.oak.video;

import io.ryanjames.oak.CustomText;
import io.ryanjames.oak.ImageFactory;
import io.ryanjames.oak.VideoFactory;
import io.ryanjames.oak.config.GlobalConfig;
import io.ryanjames.oak.midi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.ryanjames.oak.common.Constants.SECOND_AS_MS;

public class VideoFramePipeline {

    private static final Logger LOG = LoggerFactory.getLogger(VideoFramePipeline.class);

    private final GlobalConfig globalConfig;

    @Inject
    public VideoFramePipeline(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public VideoFramePipeline.Result run(MidiFile midiFile, MidiNoteExtractor.Result noteInfo, File background) {
        List<BufferedImage> imageFrames = createImageFrames(noteInfo, background);

        List<Double> durations = getFrameDurations(midiFile, noteInfo.onNotes(), noteInfo.offNotes());

        double initialFrameDuration = 2000.;
        durations.addFirst(initialFrameDuration);

        if (durations.size() < imageFrames.size()) { // TODO this is a hack to fix a bug where the last frame duration is missing
            LOG.warn("Frame durations list is shorter than image frames list. Adding last frame duration manually.");
            durations.add(2000.); // Add 2 seconds for the last frame duration
        }

        List<BufferedImage> videoFrames = createVideoFrames(imageFrames, durations);

        return new VideoFramePipeline.Result(videoFrames, durations);
    }

    public List<BufferedImage> createImageFrames(MidiNoteExtractor.Result noteInfo, File background) {
        // Get image sprites
        List<BufferedImage> ocarinaSprites = NoteToImage.mapNotesToImages(noteInfo.onNotes());

        // Construct image frame
        ImageFactory imageFactory = new ImageFactory(globalConfig.videoConfig(), background);
        return imageFactory.createVideoFrames(ocarinaSprites);
    }

    protected List<BufferedImage> createVideoFrames(
            List<BufferedImage> imageFrames, List<Double> frameDurations) {
        return VideoFactory.getVideoFrames(imageFrames, frameDurations, globalConfig.videoConfig().getFramerate());
    }

    protected List<Double> getFrameDurations(MidiFile midiFile, List<MidiNote> midiNotes, List<MidiNote> offNotes) {
        // Intro frame durations
        List<Double> frameDurations = new ArrayList<>(Collections.nCopies(CustomText.getVideoIntroText().size(),
                SECOND_AS_MS * midiFile.getTicksInMs()));

        // Played note frame durations
        List<Double> durations = TempoAwareDurations.extractDurations(midiFile.getSequence(),
                midiNotes, offNotes);
        frameDurations.addAll(durations);

        // Outro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getVideoOutroText().size(),
                SECOND_AS_MS * midiFile.getTicksInMs()));
        return frameDurations;
    }

    public record Result(List<BufferedImage> videoFrames, List<Double> frameDurationsMs) {}
}
