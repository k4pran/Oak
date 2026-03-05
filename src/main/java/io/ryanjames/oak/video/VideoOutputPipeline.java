package io.ryanjames.oak.video;

import io.ryanjames.oak.CustomText;
import io.ryanjames.oak.VideoConfig;
import io.ryanjames.oak.config.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static io.ryanjames.oak.common.Constants.SECOND_AS_MS;

public class VideoOutputPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(VideoOutputPipeline.class);

    private final GlobalConfig globalConfig;

    @Inject
    public VideoOutputPipeline(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void run(List<BufferedImage> videoFrames, List<Double> frameDurations, File audioFile, String outputDir) {
        FFMpeg ffMpeg = new FFMpeg();
        VideoConfig videoConfig = globalConfig.videoConfig();

        // Sum the initial frame duration + all intro text frame durations to get the audio offset
        int preNoteFrames = 1 + CustomText.getVideoIntroText().size(); // 1 for initial frame + intro text frames
        double audioOffset = videoConfig.getAudioOffset();
        for(int i = 0; i < preNoteFrames && i < frameDurations.size(); i++) {
            audioOffset += frameDurations.get(i) / SECOND_AS_MS;
        }
        ffMpeg.outputTutorial(videoFrames, outputDir, audioFile.getAbsolutePath(),
                videoConfig.getFramerate(), audioOffset);
    }
}
