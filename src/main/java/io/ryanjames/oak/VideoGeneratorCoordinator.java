package io.ryanjames.oak;

import io.ryanjames.oak.audio.AudioPipeline;
import io.ryanjames.oak.video.VideoFramePipeline;
import io.ryanjames.oak.midi.MidiPipeline;
import io.ryanjames.oak.video.VideoOutputPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;

public class VideoGeneratorCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(VideoGeneratorCoordinator.class);

    private final MidiPipeline midiPipeline;
    private final AudioPipeline audioPipeline;
    private final VideoFramePipeline videoFramePipeline;
    private final VideoOutputPipeline videoOutputPipeline;

    @Inject
    public VideoGeneratorCoordinator(MidiPipeline midiPipeline,
                                     AudioPipeline audioPipeline,
                                     VideoFramePipeline videoFramePipeline,
                                     VideoOutputPipeline videoOutputPipeline) {
        this.midiPipeline = midiPipeline;
        this.audioPipeline = audioPipeline;
        this.videoFramePipeline = videoFramePipeline;
        this.videoOutputPipeline = videoOutputPipeline;
    }

    public void generateVideo(File midiInput, File background) {

        // Midi creation
        MidiPipeline.Result midiPipelineResult = midiPipeline.run(midiInput);

        // Audio creation
        File audioFile = audioPipeline.run(midiPipelineResult.midiFile());

        // Video frame creation
        VideoFramePipeline.Result imagePipelineResult = videoFramePipeline.run(midiPipelineResult.midiFile(), midiPipelineResult.noteInfo(), background);

        // Video output
        videoOutputPipeline.run(imagePipelineResult.videoFrames(), imagePipelineResult.frameDurationsMs(), audioFile);

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
