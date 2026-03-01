package io.ryanjames.oak;

import io.ryanjames.oak.audio.AudioPipeline;
import io.ryanjames.oak.config.GlobalConfig;
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
    private final GlobalConfig globalConfig;

    @Inject
    public VideoGeneratorCoordinator(MidiPipeline midiPipeline,
                                     AudioPipeline audioPipeline,
                                     VideoFramePipeline videoFramePipeline,
                                     VideoOutputPipeline videoOutputPipeline,
                                     GlobalConfig globalConfig) {
        this.midiPipeline = midiPipeline;
        this.audioPipeline = audioPipeline;
        this.videoFramePipeline = videoFramePipeline;
        this.videoOutputPipeline = videoOutputPipeline;
        this.globalConfig = globalConfig;
    }

    public void generateVideo(File midiInput, File background) {

        RunArtifacts run = RunArtifacts.fromTitle(globalConfig.textConfig().getTitle(), globalConfig.outputDir());
        run.persistInput("midi", midiInput);
        run.persistInput("background", background);

        String audioInputPath = globalConfig.videoConfig().getAudioFilePath();
        if (audioInputPath != null && !audioInputPath.isBlank()) {
            run.persistInput("audio", new File(audioInputPath));
        }

        // Midi creation
        MidiPipeline.Result midiPipelineResult = midiPipeline.run(midiInput);
        run.persistOutputMidi(midiPipelineResult.midiFile());

        // Audio creation
        File audioFile = audioPipeline.run(midiPipelineResult.midiFile());
        run.persistOutputWav(audioFile);

        // Video frame creation
        VideoFramePipeline.Result imagePipelineResult = videoFramePipeline.run(midiPipelineResult.midiFile(), midiPipelineResult.noteInfo(), background);

        // Video output
        videoOutputPipeline.run(imagePipelineResult.videoFrames(), imagePipelineResult.frameDurationsMs(), audioFile, run.outputDir().toString());

        cleanUp(audioFile);
    }

    private static void cleanUp(File audioFile) {
        if (audioFile == null) {
            return;
        }
        if (audioFile.delete()) {
            LOG.info("Temporary audio file {} deleted", audioFile.getAbsolutePath());
        }
    }
}
