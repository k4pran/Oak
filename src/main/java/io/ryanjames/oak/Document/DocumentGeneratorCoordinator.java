package io.ryanjames.oak.Document;

import io.ryanjames.oak.config.GlobalConfig;
import io.ryanjames.oak.midi.MidiPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class DocumentGeneratorCoordinator {


    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorCoordinator.class);

    private final MidiPipeline midiPipeline;
    private final DocFramePipeline docFramePipeline;
    private final DocumentOutputPipeline documentOutputPipeline;
    private final GlobalConfig globalConfig;

    @Inject
    public DocumentGeneratorCoordinator(MidiPipeline midiPipeline,
                                     DocFramePipeline docFramePipeline,
                                     DocumentOutputPipeline documentOutputPipeline,
                                        GlobalConfig globalConfig) {
        this.midiPipeline = midiPipeline;
        this.docFramePipeline = docFramePipeline;
        this.documentOutputPipeline = documentOutputPipeline;
        this.globalConfig = globalConfig;
    }

    public void generateDoc(File midiInput, File background) {

        // Midi creation
        MidiPipeline.Result midiPipelineResult = midiPipeline.run(midiInput);

        // Video frame creation
        List<BufferedImage> imageFrames = docFramePipeline.createImageFrames(midiPipelineResult.noteInfo(), background);

        // Doc Pipeline
        documentOutputPipeline.run(imageFrames, globalConfig.outputDir(), globalConfig.textConfig().getTitle());

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
