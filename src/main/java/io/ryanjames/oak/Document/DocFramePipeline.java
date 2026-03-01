package io.ryanjames.oak.Document;

import io.ryanjames.oak.ImageFactory;
import io.ryanjames.oak.config.GlobalConfig;
import io.ryanjames.oak.midi.MidiFile;
import io.ryanjames.oak.midi.MidiNoteExtractor;
import io.ryanjames.oak.midi.NoteToImage;
import io.ryanjames.oak.video.VideoFramePipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class DocFramePipeline {

    private static final Logger LOG = LoggerFactory.getLogger(DocFramePipeline.class);

    private final GlobalConfig globalConfig;

    @Inject
    public DocFramePipeline(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public DocFramePipeline.Result run(MidiFile midiFile, MidiNoteExtractor.Result noteInfo, File background) {
        List<BufferedImage> imageFrames = createImageFrames(noteInfo, background);

        return new DocFramePipeline.Result(imageFrames);
    }


    public List<BufferedImage> createImageFrames(MidiNoteExtractor.Result noteInfo, File background) {
        // Get image sprites
        List<BufferedImage> ocarinaSprites = NoteToImage.mapNotesToImages(noteInfo.onNotes());

        // Construct image frame
        ImageFactory imageFactory = new ImageFactory(globalConfig.videoConfig(), background);
        return imageFactory.createDocFrames(ocarinaSprites);
    }

    public record Result(List<BufferedImage> videoFrames) {}
}
