package io.ryanjames.oak.Document;

import io.ryanjames.oak.audio.AudioPipeline;
import io.ryanjames.oak.video.VideoFramePipeline;
import io.ryanjames.oak.midi.MidiPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DocumentGeneratorCoordinator {


    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorCoordinator.class);

    private final MidiPipeline midiPipeline;
    private final DocFramePipeline docFramePipeline;
    private final DocumentOutputPipeline documentOutputPipeline;

    @Inject
    public DocumentGeneratorCoordinator(MidiPipeline midiPipeline,
                                     DocFramePipeline docFramePipeline,
                                     DocumentOutputPipeline documentOutputPipeline) {
        this.midiPipeline = midiPipeline;
        this.docFramePipeline = docFramePipeline;
        this.documentOutputPipeline = documentOutputPipeline;
    }

    public void generateDoc(File midiInput, File background) {

        // Midi creation
        MidiPipeline.Result midiPipelineResult = midiPipeline.run(midiInput);

        // Video frame creation
        List<BufferedImage> imageFrames = docFramePipeline.createImageFrames(midiPipelineResult.noteInfo(), background);

        // Doc Pipeline
        documentOutputPipeline.run(imageFrames);

        cleanUp();
    }

    private static void cleanUp() {
        String wavPath = "src/main/resources/temp/temp.wav";
        File file = new File(wavPath);
        if(file.delete()) {
            LOG.info("Temporary audio file {} deleted", wavPath);
        }
    }
}
