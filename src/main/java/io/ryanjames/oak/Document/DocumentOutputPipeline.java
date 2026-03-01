package io.ryanjames.oak.Document;

import io.ryanjames.oak.config.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DocumentOutputPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentOutputPipeline.class);

    private final GlobalConfig globalConfig;
    private final PdfOutput pdfOutput;

    @Inject
    public DocumentOutputPipeline(GlobalConfig globalConfig, PdfOutput pdfOutput) {
        this.globalConfig = globalConfig;
        this.pdfOutput = pdfOutput;
    }


    public void run(List<BufferedImage> imageFrames) {
        String outputDest = Paths.get(globalConfig.outputDir(), globalConfig.textConfig().getTitle() + ".pdf").toString();
        this.pdfOutput.writePdf(outputDest, imageFrames);
    }
}
